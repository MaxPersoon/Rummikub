package Main;

import Players.Player;
import Players.RandomPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

    private static final String[] PLAYER_TYPES = {"random", "random"};

    private static final List<Tile> TILES = new ArrayList<>();
    public static final List<Set> SETS = new ArrayList<>();
    public static final List<Player> PLAYERS = new ArrayList<>();
    private static GameState currentState;

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        initialize();
        gameLoop();
    }

    private static void initialize() {
        // Create players
        for (String playerType : PLAYER_TYPES) {
            if (playerType.equals("random")) {
                PLAYERS.add(new RandomPlayer(PLAYERS.size() + 1));
            }
        }

        // Create tiles
        HashMap<String, List<Tile>> tilesWithColour = new HashMap<>();
        HashMap<Integer, List<Tile>> tilesWithNumber = new HashMap<>();

        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            tilesWithColour.put(colour, new ArrayList<>());
            for (int number = 1; number <= 13; number++) {
                Tile tile = new Tile(TILES.size() + 1, number, colour, PLAYERS);
                TILES.add(tile);
                tilesWithColour.get(colour).add(tile);
                if (!tilesWithNumber.containsKey(number)) {
                    tilesWithNumber.put(number, new ArrayList<>());
                }
                tilesWithNumber.get(number).add(tile);
            }
        }

        // Make a copy of each tile
        List<Tile> tilesCopy = List.copyOf(TILES);
        for (Tile tile : tilesCopy) {
            TILES.add(tile.makeCopy(TILES.size() + 1));
        }

//        for (Tile tile : TILES) {
//            tile.print();
//        }

        // Create groups
        for (int i = 1; i <= 13; i++) {
            List<Tile> tilesWithSpecificNumber = tilesWithNumber.get(i);

            Set set = new Set(SETS.size() + 1, "group", tilesWithSpecificNumber);
            SETS.add(set);

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
        HashMap<Player, List<Tile>> racks = new HashMap<>();
        List<Set> table = new ArrayList<>();
        List<Tile> pool = new ArrayList<>(List.copyOf(TILES));

        // Randomly give each player fourteen tiles
        for (int i = 0; i < 14; i++) {
            for (Player player : PLAYERS) {
                if (!racks.containsKey(player)) {
                    racks.put(player, new ArrayList<>());
                }
                int randomIndex = (int) (Math.random() * pool.size());
                racks.get(player).add(pool.remove(randomIndex));
            }
        }

        currentState = new GameState(racks, table, pool);
        currentState.printRacks();
        currentState.printTable();
        //visualizeCurrentState();
    }

    private static void gameLoop() {
        // while no player has won {
        //   for every player {
        //     player determines next game state
        //     visualize new game state
        //     stop if game state is a winning state
        //   }
        // }
        int turnCounter = 0;
        Player winner = null;
        while (winner == null) {
            turnCounter++;
            System.out.println("\n --- TURN #" + turnCounter + " ---");
            for (Player player : PLAYERS) {
                player.unstuck();
            }
            for (Player player : PLAYERS) {
                currentState = player.makeMove(currentState);
                currentState.printRack(player);
                currentState.printTable();
                //visualizeCurrentState();
                if (player.checkWin(currentState)) {
                    // Player has empty rack --> wins
                    winner = player;
                    break;
                }
                else {
                    boolean allStuck = true;
                    int smallestRackSize = Integer.MAX_VALUE;
                    Player playerWithSmallestRackSize = null;
                    for (Player player1 : PLAYERS) {
                        if (!player1.isStuck()) {
                            allStuck = false;
                            break;
                        }
                        else {
                            int playerRackSize = currentState.getRACKS().get(player).size();
                            if (playerRackSize < smallestRackSize) {
                                smallestRackSize = playerRackSize;
                                playerWithSmallestRackSize = player1;
                            }
                        }
                    }

                    if (allStuck) {
                        // All players are stuck --> player with the smallest rack size wins
                        winner = playerWithSmallestRackSize;
                        break;
                    }
                }

//                int counter = 0;
//                while (counter < 1000000000) {
//                    counter++;
//                }
            }
        }

        System.out.println("Player #" + winner.getID() + " wins");
    }

    private static void visualizeCurrentState() {

    }

}
