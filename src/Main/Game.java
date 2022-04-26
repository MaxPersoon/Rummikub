package Main;

import Players.GreedyPlayer;
import Players.Player;
import Players.RandomPlayer;
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

    private static void initialize() {
        // Create players
        for (String playerType : Main.PLAYER_TYPES) {
            if (playerType.equals("random")) {
                PLAYERS.add(new RandomPlayer(PLAYERS.size() + 1));
            }
            else if (playerType.equals("greedy")) {
                PLAYERS.add(new GreedyPlayer(PLAYERS.size() + 1));
            }
        }

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

//        for (Tile tile : TILES) {
//            tile.print();
//        }

        // Create groups
        for (int i = 1; i <= 13; i++) {
            List<Tile> tilesWithSpecificNumber = tilesWithNumber.get(i);

            // Size 4
            Set set = new Set(SETS.size() + 1, "group", tilesWithSpecificNumber);
            SETS.add(set);

            // Size 3
            for (Tile tile : tilesWithSpecificNumber) {
                List<Tile> tilesWithSpecificNumberCopy = new ArrayList<>(List.copyOf(tilesWithSpecificNumber));
                tilesWithSpecificNumberCopy.remove(tile);

                set = new Set(SETS.size() + 1, "group", tilesWithSpecificNumberCopy);
                SETS.add(set);
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
                    }
                }
            }
        }

//        for (Set set : SETS) {
//            set.print();
//        }

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

        currentState = new GameState(null, racks, table, pool);
        currentState.printRacks();
        currentState.visualize();
    }

    private static void gameLoop() {
        int turnCounter = 0;
        List<Player> winners = new ArrayList<>();

        while (winners.isEmpty()) {
            turnCounter++;
            System.out.println("\n--- TURN #" + turnCounter + " ---");

            for (Player player : PLAYERS) {
                player.unstuck();

                System.out.println("/ Player #" + player.getID() + " \\");
                PREVIOUS_STATES.add(currentState);
                currentState = player.makeMove(currentState);
                currentState.visualize();

                if (player.checkWin(currentState)) {
                    // Player has empty rack --> wins
                    winners.add(player);
                    break;
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
            System.out.println("Player #" + winners.get(0).getID() + " wins");
        }
        else {
            System.out.println("It's a tie!\nWinners:");
            for (Player winner : winners) {
                System.out.println("- Player #" + winner.getID());
            }
        }
    }

}
