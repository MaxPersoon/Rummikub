package Main;

import Players.Player;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GameState {

    private final GameState PARENT;
    private final LinkedHashMap<Player, List<Tile>> RACKS;
    private final LinkedHashMap<Set, List<Tile>> TABLE;
    private final List<Tile> POOL;

    public GameState(GameState parent, LinkedHashMap<Player, List<Tile>> racks, LinkedHashMap<Set, List<Tile>>  table, List<Tile> pool) {
        this.PARENT = parent;
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
    }

    public GameState getPARENT() {
        return PARENT;
    }

    public LinkedHashMap<Player, List<Tile>> getRACKS() {
        return RACKS;
    }

    public LinkedHashMap<Set, List<Tile>> getTABLE() {
        return TABLE;
    }

    public List<Tile> getPOOL() {
        return POOL;
    }

    public List<GameState> getMoves(Player player) {
        List<GameState> moves = new ArrayList<>();
        List<Tile> playerRack = this.RACKS.get(player);

        // Draw sets from rack on table
        for (Set set : Game.SETS) {
            List<Tile> tilesToRemove = set.drawableFromRack(playerRack);

            if (tilesToRemove.size() >= 1) {
                // Create new GameState
                List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
                for (Tile tileToRemove : tilesToRemove) {
                    newPlayerRack.remove(tileToRemove);
                }

                LinkedHashMap<Player, List<Tile>> newRacks = new LinkedHashMap<>(RACKS);
                newRacks.replace(player, newPlayerRack);

                LinkedHashMap<Set, List<Tile>> newTable = new LinkedHashMap<>(TABLE);
                newTable.put(set, tilesToRemove);

                List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

                moves.add(new GameState(this, newRacks, newTable, newPool));
            }
        }

        // Add tile from rack to 1) existing group and 2) front and back of existing run
//        for (Tile playerTile : playerRack) {
//            for (Set set : TABLE) {
//                if (set.isExpandingTile(playerTile)) {
//                    // Create new GameState
//                    List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
//                    newPlayerRack.remove(playerTile);
//
//                    HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
//                    newRacks.replace(player, newPlayerRack);
//
//                    List<Tile> newTilesSet = new ArrayList<>(List.copyOf(tilesSet));
//                    newTilesSet.add(playerTile);
//
//                    List<Set> newTable = new ArrayList<>(List.copyOf(TABLE));
//                    newTable.remove(set);
//                    for (Set set1 : Game.SETS) {
//                        if (set1.checkMatchingTiles(newTilesSet)) {
//                            newTable.add(set1);
//                            break;
//                        }
//                    }
//
//                    List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));
//
//                    moves.add(new GameState(newRacks, newTable, newPool));
//                }
//            }
//        }

        // Filter out moves in which an opponent wins
        List<GameState> filteredMoves = new ArrayList<>();
        for (GameState move : moves) {
            HashMap<Player, List<Tile>> racks = move.getRACKS();
            boolean keep = true;
            for (Player player_ : racks.keySet()) {
                if (racks.get(player_).size() == 0) {
                    if (player_ != player) {
                        keep = false;
                        break;
                    }
                }
            }

            if (keep) {
                filteredMoves.add(move);
            }
        }
        moves = filteredMoves;

        // If the player cannot make a move, draw a tile from the pool (if possible)
        if (moves.size() == 0) {
            if (POOL.size() >= 1) {
                List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));
                Tile tileFromPool = newPool.remove(0);

                List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
                newPlayerRack.add(tileFromPool);
                System.out.println("Draws {" + tileFromPool.getNUMBER() + ", " + tileFromPool.getCOLOUR() + "} from the pool");

                LinkedHashMap<Player, List<Tile>> newRacks = new LinkedHashMap<>(RACKS);
                newRacks.replace(player, newPlayerRack);

                LinkedHashMap<Set, List<Tile>> newTable = new LinkedHashMap<>(TABLE);

                moves.add(new GameState(this, newRacks, newTable, newPool));
            }
        }

        return moves;
    }

    public void printRacks() {
        for (Player player : RACKS.keySet()) {
            System.out.print("* Player #" + player.getID() + "'s rack: ");
            printRack(player);
        }
    }

    public void printRack(Player player) {
        StringBuilder text = new StringBuilder();
        for (Tile tile : RACKS.get(player)) {
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
        }
        System.out.println(text);
    }

    public void printTable() {
        System.out.println("Table:");
        for (Set set : TABLE.keySet()) {
            StringBuilder text = new StringBuilder();
            for (Tile tile : TABLE.get(set)) {
                if (!text.isEmpty()) {
                    text.append(" | ");
                }
                text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
            }
            System.out.println("* Set #" + set.getID() + ": " + text);
        }
    }

    public void printMoveInfo(Player player) {
        // Print new sets
        List<Set> newSets = new ArrayList<>();
        for (Set set : TABLE.keySet()) {
            boolean newSet = true;
            for (Set setParent : PARENT.TABLE.keySet()) {
                if (set == setParent) {
                    newSet = false;
                    break;
                }
            }

            if (newSet) {
                newSets.add(set);
            }
        }

        if (newSets.size() >= 1) {
            System.out.println("New sets:");
            for (Set newSet : newSets) {
                newSet.print();
            }
        }

        System.out.println("Rack:");
        printRack(player);

        System.out.println();
    }

    public void visualize() {
        // Reset board
        for (Tile tile : Game.TILES) {
            tile.getImage().setVisible(false);
        }

        // Racks
        for (Player player : RACKS.keySet()) {
            List<Tile> rack = RACKS.get(player);
            List<double[]> coordinates = Main.COORDINATES_RACKS.get(player.getID());
            for (int i = 0; i < rack.size(); i++) {
                rack.get(i).setImageCoordinates(coordinates.get(i));
            }
        }

        // Sets
        List<List<Tile>> sets = TABLE.values().stream().toList();
        for (int set = 0; set < sets.size(); set++) {
            List<Tile> tilesInSet = sets.get(set);
            List<double[]> coordinates = Main.COORDINATES_TABLE.get(set);

            for (int tile = 0; tile < tilesInSet.size(); tile++) {
                tilesInSet.get(tile).setImageCoordinates(coordinates.get(tile));
            }
        }

        // Pool counter
        Text counter = (Text) Game.nodes.get(0);
        counter.setText(String.valueOf(POOL.size()));
    }


}
