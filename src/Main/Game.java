package Main;

import Players.*;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import java.util.*;

public class Game extends Thread {

    public static final List<Tile> TILES = new ArrayList<>();
    public static final List<Set> SETS = new ArrayList<>();
    public static final List<Player> PLAYERS = new ArrayList<>();
    public static final List<GameState> PREVIOUS_STATES = new ArrayList<>();
    public static GameState currentState;
    public static List<Node> nodes;

    public void run() {
        initialize();
        gameLoop();
    }

    private void initialize() {
        // Create tiles
        HashMap<String, List<Tile>> tilesWithColour = new HashMap<>();
        HashMap<Integer, List<Tile>> tilesWithNumber = new HashMap<>();

        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            tilesWithColour.put(colour, new ArrayList<>());
            for (int number = 1; number <= 13; number++) {
                Tile copy1 = new Tile(TILES.size() + 1, number, colour, (ImageView) nodes.get(TILES.size() + 1));
                Tile copy2 = new Tile(TILES.size() + 2, number, colour, (ImageView) nodes.get(TILES.size() + 2));
                TILES.add(copy1);
                TILES.add(copy2);
                tilesWithColour.get(colour).add(copy1);
                if (!tilesWithNumber.containsKey(number)) {
                    tilesWithNumber.put(number, new ArrayList<>());
                }
                tilesWithNumber.get(number).add(copy1);
            }
        }

        Tile jokerCopy1 = new Tile(TILES.size() + 1, 30, "joker", (ImageView) nodes.get(TILES.size() + 1));
        Tile jokerCopy2 = new Tile(TILES.size() + 2, 30, "joker", (ImageView) nodes.get(TILES.size() + 2));
        TILES.add(jokerCopy1);
        TILES.add(jokerCopy2);

//        for (Tile tile : TILES) {
//            tile.print();
//        }

        // Create groups
        for (int i = 1; i <= 13; i++) {
            List<Tile> tilesWithSpecificNumber = tilesWithNumber.get(i);

            // Size 4
            Set set = new Set(SETS.size() + 1, "group", tilesWithSpecificNumber);
            SETS.add(set);
            putJokersInSet("group", tilesWithSpecificNumber);

            // Size 3
            for (Tile tile : tilesWithSpecificNumber) {
                List<Tile> tilesWithSpecificNumberCopy = new ArrayList<>(List.copyOf(tilesWithSpecificNumber));
                tilesWithSpecificNumberCopy.remove(tile);

                set = new Set(SETS.size() + 1, "group", tilesWithSpecificNumberCopy);
                SETS.add(set);
                putJokersInSet("group", tilesWithSpecificNumberCopy);
            }
        }

        // Create runs
        for (String colour : colours) {
            List<Tile> tilesWithSpecificColour = tilesWithColour.get(colour);

            while (tilesWithSpecificColour.size() >= 3) {
                List<Tile> run = new ArrayList<>();
                run.add(tilesWithSpecificColour.remove(0));
                for (Tile tile : tilesWithSpecificColour) {
                    run.add(tile);
                    if (run.size() >= 3) {
                        Set set = new Set(SETS.size() + 1, "run", run);
                        SETS.add(set);
                        putJokersInSet("run", run);
                    }
                }
            }
        }

//        for (Set set : SETS) {
//            set.print();
//        }

        // Create players
        for (int i = 0; i < Main.PLAYER_TYPES.length; i++) {
            int id = PLAYERS.size() + 1;
            String playerType = Main.PLAYER_TYPES[i];
            String objectiveFunction = Main.OBJECTIVE_FUNCTIONS[i];

            switch (playerType) {
                case "random" -> PLAYERS.add(new RandomPlayer(id, objectiveFunction));
                case "greedy" -> PLAYERS.add(new GreedyPlayer(id, objectiveFunction));
                case "ilp" -> PLAYERS.add(new IntegerLinearProgramming(id, objectiveFunction));
                case "alphabeta" -> PLAYERS.add(new AlphaBetaPlayer(id, objectiveFunction));
                default -> {
                    System.out.println("Error: invalid player type \"" + playerType + "\"");
                    System.exit(0);
                }
            }
        }

        // Create initial GameState
        LinkedHashMap<Player, List<Tile>> racks = new LinkedHashMap<>();
        LinkedHashMap<Set, List<List<Tile>>> table = new LinkedHashMap<>();
        List<Tile> pool = new ArrayList<>(List.copyOf(TILES));
        Collections.shuffle(pool);

        for (Player player : PLAYERS) {
            racks.put(player, new ArrayList<>());
        }

        // Randomly give each player fourteen tiles
        for (int i = 0; i < 14; i++) {
            for (Player player : PLAYERS) {
                int randomIndex = (int) (Math.random() * pool.size());
                racks.get(player).add(pool.remove(randomIndex));
            }
        }

        // Force a setup
//        String player1CustomRack = "30 joker,30 joker,1 blue";
//        String player2CustomRack = "1 black,1 red,1 orange";
//
//        List<String> customRacks = new ArrayList<>();
//        customRacks.add(player1CustomRack);
//        customRacks.add(player2CustomRack);
//        customSetup(customRacks, racks, pool);

        currentState = new GameState(racks, table, pool);
        currentState.printRacks();
        currentState.visualize();
    }

    private void putJokersInSet(String type, List<Tile> tiles) {
        Tile joker = TILES.get(TILES.size() - 2);

        for (int i = 0; i < tiles.size(); i++) {
            List<Tile> tilesOneJoker = new ArrayList<>(List.copyOf(tiles));
            tilesOneJoker.set(i, joker);
            Set jokerSet = new Set(SETS.size() + 1, type, tilesOneJoker);
            SETS.add(jokerSet);

            for (int j = i + 1; j < tiles.size(); j++) {
                List<Tile> tilesTwoJokers = new ArrayList<>(List.copyOf(tilesOneJoker));
                tilesTwoJokers.set(j, joker);
                jokerSet = new Set(SETS.size() + 1, type, tilesTwoJokers);
                SETS.add(jokerSet);
            }
        }
    }

    private void customSetup(List<String> customRacks, LinkedHashMap<Player, List<Tile>> racks, List<Tile> pool) {
        for (int i = 0; i < customRacks.size(); i++) {
            Player player = PLAYERS.get(i);
            String customRack = customRacks.get(i);

            String[] tiles = customRack.split(",");
            for (String tile : tiles) {
                String[] attributes = tile.split(" ");
                int tileNumber = Integer.parseInt(attributes[0]);
                String tileColour = attributes[1];

                for (Tile poolTile : pool) {
                    if (poolTile.getNUMBER() == tileNumber && poolTile.getCOLOUR().equals(tileColour)) {
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

            for (Player player : PLAYERS) {
                player.unstuck();

                System.out.println("/ Player #" + player.getID() + " (" + player.getName() + " w/ " + player.getObjectiveFunction() + ") \\");
                long startTime = System.currentTimeMillis();
                GameState newState = player.makeMove(currentState);
                long endTime = System.currentTimeMillis();

                // If the player cannot make a move, draw a tile from the pool (if possible)
                if (newState == currentState) {
                    if (currentState.getPOOL().size() >= 1) {
                        newState = currentState.createChild();
                        newState.drawTileFromPool(player);
                    }
                    else {
                        System.out.println("Player #" + player.getID() + " is unable to make a move\n");
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
                    System.out.println("Time needed (ms): " + (endTime - startTime) + "\n");
                    newState.visualize();

                    PREVIOUS_STATES.add(currentState);
                    currentState = newState;
                    currentState.setDepth(1);

                    if (player.checkWin(currentState)) {
                        // Player has empty rack --> wins
                        winners.add(player);
                        break;
                    }
                }

                // Delay between players making moves
                int counter = 0;
                while (counter < 100000000) {
                    counter++;
                }
            }

            // Additional win scenario: all players are stuck
            boolean allStuck = true;
            List<Player> playersWithSmallestRackSize = new ArrayList<>();
            int smallestRackSize = Integer.MAX_VALUE;
            for (Player player : PLAYERS) {
                if (!player.isStuck()) {
                    allStuck = false;
                    break;
                }
                else {
                    int playerRackSize = currentState.getRACKS().get(player).size();
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

        if (winners.size() == 1) {
            Player winner = winners.get(0);
            System.out.println("Player #" + winner.getID() + " (" + winner.getName() + " w/ " + winner.getObjectiveFunction() + ") wins!");
        }
        else {
            System.out.println("It's a tie!\nWinners:");
            for (Player winner : winners) {
                System.out.println("- Player #" + winner.getID() + " (" + winner.getName() + " w/ " + winner.getObjectiveFunction() + ")");
            }
        }
    }

    public static Player nextPlayer(Player currentPlayer) {
        Player nextPlayer = null;

        for (int i = 0; i < PLAYERS.size(); i++) {
            Player player = PLAYERS.get(i);

            if (player == currentPlayer) {
                if (i + 1 < PLAYERS.size()) {
                    // List goes on --> next player is next entry in list
                    nextPlayer = PLAYERS.get(i + 1);
                }
                else {
                    // Reached end of list --> next player is first entry in list
                    nextPlayer = PLAYERS.get(0);
                }
                break;
            }
        }

        return nextPlayer;
    }

}
