package Main;

import Experiments.runExperiments;
import Players.*;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import java.util.*;

public class Game extends Thread {

    public static boolean finetuning = false;

    public static final int maximumMoveTime = 60000; // ms

    public static String[] playerTypes;
    public static String[] objectiveFunctions;
    public static boolean experimenting;
    public static List<Node> nodes;

    public static List<Tile> tiles;
    public static List<Set> sets;
    public static List<Player> players;
    public static List<GameState> previousStates;
    public static GameState currentState;

    public void run() {
        initialize();
        gameLoop();
    }

    private void initialize() {
        tiles = new ArrayList<>();
        sets = new ArrayList<>();
        players = new ArrayList<>();
        previousStates = new ArrayList<>();

        // Create default tiles
        HashMap<String, List<Tile>> tilesWithColour = new HashMap<>();
        HashMap<Integer, List<Tile>> tilesWithNumber = new HashMap<>();

        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            tilesWithColour.put(colour, new ArrayList<>());
            for (int number = 1; number <= 13; number++) {
                Tile copy1 = new Tile(tiles.size() + 1, number, colour);
                Tile copy2 = new Tile(tiles.size() + 2, number, colour);

                if (!experimenting && !finetuning) {
                    copy1.setImage((ImageView) nodes.get(tiles.size() + 1));
                    copy2.setImage((ImageView) nodes.get(tiles.size() + 2));
                }

                tiles.add(copy1);
                tiles.add(copy2);

                tilesWithColour.get(colour).add(copy1);
                if (!tilesWithNumber.containsKey(number)) {
                    tilesWithNumber.put(number, new ArrayList<>());
                }
                tilesWithNumber.get(number).add(copy1);
            }
        }

        // Create joker tiles
        Tile jokerCopy1 = new Tile(tiles.size() + 1, 30, "joker");
        Tile jokerCopy2 = new Tile(tiles.size() + 2, 30, "joker");

        if (!experimenting && !finetuning) {
            jokerCopy1.setImage((ImageView) nodes.get(tiles.size() + 1));
            jokerCopy2.setImage((ImageView) nodes.get(tiles.size() + 2));
        }

        tiles.add(jokerCopy1);
        tiles.add(jokerCopy2);

        // Debugging: print each tile's info
//        for (Tile tile : tiles) {
//            tile.print();
//        }

        // Create sets
        // Create groups
        for (int i = 1; i <= 13; i++) {
            List<Tile> tilesWithSpecificNumber = tilesWithNumber.get(i);

            // Size 4
            addNewSet("group", tilesWithSpecificNumber);
            putJokersInGroup(tilesWithSpecificNumber);

            // Size 3
            for (Tile tile : tilesWithSpecificNumber) {
                List<Tile> tilesWithSpecificNumberCopy = new ArrayList<>(List.copyOf(tilesWithSpecificNumber));
                tilesWithSpecificNumberCopy.remove(tile);

                addNewSet("group", tilesWithSpecificNumberCopy);
                putJokersInGroup(tilesWithSpecificNumberCopy);
            }
        }

        // Create runs
        for (String colour : colours) {
            List<Tile> tilesWithSpecificColour = tilesWithColour.get(colour);

            while (tilesWithSpecificColour.size() >= 3) {
                List<Tile> run = new ArrayList<>();
                run.add(tilesWithSpecificColour.remove(0));

                int numberOfIterations = Math.min(tilesWithSpecificColour.size(), 4);
                for (int i = 0; i < numberOfIterations; i++) {
                    run.add(tilesWithSpecificColour.get(i));
                    if (run.size() >= 3) {
                        addNewSet("run", run);
                        putJokersInRun(run);
                    }
                }
            }
        }

        // Debugging: print each set's info
//        for (Set set : sets) {
//            set.print();
//        }

        // Create players
        for (int i = 0; i < playerTypes.length; i++) {
            int id = players.size() + 1;
            String playerType = playerTypes[i];
            String objectiveFunction = objectiveFunctions[i];

            switch (playerType) {
                case "greedy" -> players.add(new GreedyPlayer(id, objectiveFunction));
                case "alphabeta" -> players.add(new AlphaBetaPlayer(id, objectiveFunction));
                case "ilp" -> players.add(new IntegerLinearProgramming(id, objectiveFunction));
                default -> {
                    System.out.println("Error: invalid player type \"" + playerType + "\"");
                    System.exit(0);
                }
            }
        }

        // Create initial GameState
        LinkedHashMap<Player, List<Tile>> racks = new LinkedHashMap<>();
        LinkedHashMap<Set, List<List<Tile>>> table = new LinkedHashMap<>();
        List<Tile> pool = new ArrayList<>(List.copyOf(tiles));
        Collections.shuffle(pool);

        for (Player player : players) {
            racks.put(player, new ArrayList<>());
        }

        // Randomly give each player fourteen tiles
        for (int i = 0; i < 14; i++) {
            for (Player player : players) {
                int randomIndex = (int) (Math.random() * pool.size());
                racks.get(player).add(pool.remove(randomIndex));
            }
        }

        // Force a setup
        // NOTE: Disable for-loop above
//        String player1CustomRack = "30 joker,30 joker,1 blue";
//        String player2CustomRack = "1 black,1 red,1 orange";
//
//        List<String> customRacks = new ArrayList<>();
//        customRacks.add(player1CustomRack);
//        customRacks.add(player2CustomRack);
//        customSetup(customRacks, racks, pool);

        currentState = new GameState(racks, table, pool);
        currentState.printRacks();
        if (!experimenting && !finetuning) {
            currentState.visualize();
        }
    }

    private void addNewSet(String type, List<Tile> tiles) {
        StringBuilder signatureBuilder = new StringBuilder();
        for (Tile tile : tiles) {
            if (!signatureBuilder.isEmpty()) {
                signatureBuilder.append(";");
            }
            signatureBuilder.append(tile.getNumber()).append(",").append(tile.getColour());
        }
        signatureBuilder.append("-").append(type);
        String signature = signatureBuilder.toString();

        boolean duplicate = false;
        for (Set set : sets) {
            if (set.getSignature().equals(signature)) {
                // Duplicate set
                duplicate = true;
                break;
            }
        }

        if (!duplicate) {
            sets.add(new Set(sets.size() + 1, type, tiles, signature));
        }
    }

    private void putJokersInGroup(List<Tile> tilesInSet) {
        Tile joker = tiles.get(tiles.size() - 2);

        for (int i = 0; i < tilesInSet.size(); i++) {
            List<Tile> tilesOneJoker = new ArrayList<>(List.copyOf(tilesInSet));
            tilesOneJoker.remove(i);
            tilesOneJoker.add(joker);
            addNewSet("group", tilesOneJoker);

            for (int j = 0; j < tilesInSet.size() - 1; j++) {
                List<Tile> tilesTwoJokers = new ArrayList<>(List.copyOf(tilesOneJoker));
                tilesTwoJokers.remove(j);
                tilesTwoJokers.add(joker);
                addNewSet("group", tilesTwoJokers);
            }
        }
    }

    private void putJokersInRun(List<Tile> tilesInSet) {
        Tile joker = tiles.get(tiles.size() - 2);

        for (int i = 0; i < tilesInSet.size(); i++) {
            List<Tile> tilesOneJoker = new ArrayList<>(List.copyOf(tilesInSet));
            tilesOneJoker.set(i, joker);
            addNewSet("run", tilesOneJoker);

            for (int j = i + 1; j < tilesInSet.size(); j++) {
                List<Tile> tilesTwoJokers = new ArrayList<>(List.copyOf(tilesOneJoker));
                tilesTwoJokers.set(j, joker);
                addNewSet("run", tilesTwoJokers);
            }
        }
    }

    private void customSetup(List<String> customRacks, LinkedHashMap<Player, List<Tile>> racks, List<Tile> pool) {
        for (int i = 0; i < customRacks.size(); i++) {
            Player player = players.get(i);
            String customRack = customRacks.get(i);

            String[] tiles = customRack.split(",");
            for (String tile : tiles) {
                String[] attributes = tile.split(" ");
                int tileNumber = Integer.parseInt(attributes[0]);
                String tileColour = attributes[1];

                for (Tile poolTile : pool) {
                    if (poolTile.getNumber() == tileNumber && poolTile.getColour().equals(tileColour)) {
                        racks.get(player).add(poolTile);
                        pool.remove(poolTile);
                        break;
                    }
                }
            }
        }
    }

    private void gameLoop() {
        int turnCounter = 0;
        List<Player> winners = new ArrayList<>();

        while (winners.isEmpty()) {
            turnCounter++;
            System.out.println("\n--- TURN #" + turnCounter + " ---");

            for (Player player : players) {
                player.unstuck();

                System.out.println("/ Player #" + player.getId() + " (" + player.getName() + " w/ " + player.getObjectiveFunction() + ") \\");
                long startTime = System.currentTimeMillis();
                GameState newState = player.makeMove(currentState);
                long endTime = System.currentTimeMillis();
                long computationTime = endTime - startTime;

                // If the player cannot make a move, draw a tile from the pool (if possible)
                if (newState == currentState) {
                    if (currentState.getPool().size() >= 1) {
                        newState = new GameState(currentState);
                        newState.drawTileFromPool(player);
                    }
                    else {
                        currentState.setScore(0);
                        System.out.println("Player #" + player.getId() + " is unable to make a move\n");
                        player.stuck();
                    }
                }

                if (newState != currentState) {
                    newState.setParent(currentState); // IMPORTANT FOR PROPER TERMINAL OUTPUT
                    newState.printMoveInfo(player);
                    double score = newState.getScore();
                    if (score > 0) {
                        System.out.println("Move score: " + score);
                    }
                    System.out.println("Time needed (ms): " + computationTime + "\n");
                    if (!experimenting && !finetuning) {
                        newState.visualize();
                    }

                    previousStates.add(currentState);
                    currentState = newState;
                    currentState.setDepth(0);
                }

                if (experimenting) {
                    runExperiments.writeMoveToFile(player.getName(), player.getObjectiveFunction(), currentState.getScore(), computationTime);
                }

                if (player.checkWin(currentState)) {
                    // Player has empty rack --> wins
                    winners.add(player);
                    break;
                }

                if (!experimenting && !finetuning) {
                    // Delay between players making moves
                    long startDelay = System.currentTimeMillis();
                    while (true) {
                        if (System.currentTimeMillis() - startDelay > 100) {
                            break;
                        }
                    }
                }
            }

            // Additional win scenario: all players are stuck
            boolean allStuck = true;
            List<Player> playersWithSmallestRackSize = new ArrayList<>();
            int smallestRackSize = Integer.MAX_VALUE;
            for (Player player : players) {
                if (!player.isStuck()) {
                    allStuck = false;
                    break;
                }
                else {
                    int playerRackSize = currentState.getRacks().get(player).size();
                    if (playerRackSize < smallestRackSize) {
                        playersWithSmallestRackSize = new ArrayList<>();
                        playersWithSmallestRackSize.add(player);
                        smallestRackSize = playerRackSize;
                    }
                    else if (playerRackSize == smallestRackSize) {
                        playersWithSmallestRackSize.add(player);
                    }
                }
            }

            if (allStuck) {
                // All players are stuck --> players with the smallest rack size win
                winners = playersWithSmallestRackSize;
                break;
            }
        }

        if (experimenting) {
            runExperiments.writeWinnersToFile(winners);
        }

        if (winners.size() == 1) {
            Player winner = winners.get(0);
            System.out.println("Player #" + winner.getId() + " (" + winner.getName() + " w/ " + winner.getObjectiveFunction() + ") wins!");
        }
        else {
            System.out.println("It's a tie!\nWinners:");
            for (Player winner : winners) {
                System.out.println("- Player #" + winner.getId() + " (" + winner.getName() + " w/ " + winner.getObjectiveFunction() + ")");
            }
        }
    }

    public static Player nextPlayer(Player currentPlayer) {
        Player nextPlayer = null;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            if (player == currentPlayer) {
                if (i + 1 < players.size()) {
                    // List goes on --> next player is next entry in list
                    nextPlayer = players.get(i + 1);
                }
                else {
                    // Reached end of list --> next player is first entry in list
                    nextPlayer = players.get(0);
                }
                break;
            }
        }

        return nextPlayer;
    }

}
