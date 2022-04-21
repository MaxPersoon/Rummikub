package Main;

import Players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {

    private final HashMap<Player, List<Tile>> RACKS;
    private final HashMap<Set, List<Tile>> TABLE;
    private final List<Tile> POOL;

    public GameState(HashMap<Player, List<Tile>> racks, HashMap<Set, List<Tile>>  table, List<Tile> pool) {
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
    }

    public HashMap<Player, List<Tile>> getRACKS() {
        return RACKS;
    }

    public HashMap<Set, List<Tile>> getTABLE() {
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

                HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
                newRacks.replace(player, newPlayerRack);

                HashMap<Set, List<Tile>> newTable = new HashMap<>(TABLE);
                newTable.put(set, tilesToRemove);

                List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

                moves.add(new GameState(newRacks, newTable, newPool));
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

        // If the player cannot make a move, draw a tile from the pool (if possible)
        if (moves.size() == 0) {
            if (POOL.size() >= 1) {
                System.out.println("Player #" + player.getID() + " has drawn from the pool");
                List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

                List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
                newPlayerRack.add(newPool.remove(0));

                HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
                newRacks.replace(player, newPlayerRack);

                HashMap<Set, List<Tile>> newTable = new HashMap<>(TABLE);

                moves.add(new GameState(newRacks, newTable, newPool));
            }
        }

        return moves;
    }

    public void printRacks() {
        for (Player player : RACKS.keySet()) {
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
        System.out.println("* Player #" + player.getID() + "'s rack: " + text);
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

    public void visualize() {
        // Reset board
        for (Tile tile : Game.TILES) {
            tile.getImage().setVisible(false);
        }

        // Racks
        for (int playerID = 1; playerID <= 2; playerID++) {
            for (Player player : RACKS.keySet()) {
                if (playerID == player.getID()) {
                    List<Tile> rack = RACKS.get(player);
                    List<double[]> coordinates = Main.COORDINATES_RACKS.get(playerID);
                    for (int i = 0; i < rack.size(); i++) {
                        rack.get(i).setImageCoordinates(coordinates.get(i));
                    }
                    break;
                }
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
    }


}
