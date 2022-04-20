package Main;

import Players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {

    private final HashMap<Player, List<Tile>> RACKS;
    private final List<Set> TABLE;
    private final List<Tile> POOL;

    public GameState(HashMap<Player, List<Tile>> racks, List<Set> table, List<Tile> pool) {
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
    }

    public HashMap<Player, List<Tile>> getRACKS() {
        return RACKS;
    }

    public List<Set> getTABLE() {
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
            List<Tile> tilesSet = set.getTILES();
            List<Tile> tilesToRemove = new ArrayList<>();
            if (playerRack.size() >= tilesSet.size()) {
                boolean matchingSet = true;
                for (Tile tileSet : tilesSet) {
                    boolean matchingTile = false;
                    for (Tile playerTile : playerRack) {
                        if (tileSet.checkMatchingTile(playerTile)) {
                            tilesToRemove.add(playerTile);
                            matchingTile = true;
                            break;
                        }
                    }
                    if (!matchingTile) {
                        matchingSet = false;
                        break;
                    }
                }

                if (matchingSet) {
                    // Create new GameState
                    List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
                    for (Tile tileToRemove : tilesToRemove) {
                        newPlayerRack.remove(tileToRemove);
                    }

                    HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
                    newRacks.replace(player, newPlayerRack);

                    List<Set> newTable = new ArrayList<>(List.copyOf(TABLE));
                    newTable.add(set);

                    List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

                    moves.add(new GameState(newRacks, newTable, newPool));
                }
            }
        }

        // Add tile from rack to 1) existing group and 2) front and back of existing run
        for (Tile playerTile : playerRack) {
            for (Set set : TABLE) {
                List<Tile> tilesSet = set.getTILES();

                if (set.getTYPE().equals("group")) {
                    if (tilesSet.size() == 3) {
                        if (playerTile.getNUMBER() == tilesSet.get(0).getNUMBER()) {
                            boolean newColour = true;
                            for (Tile tileSet : tilesSet) {
                                if (playerTile.getCOLOUR().equals(tileSet.getCOLOUR())) {
                                    newColour = false;
                                    break;
                                }
                            }

                            if (newColour) {
                                // Create game state
                                createGameStateForExistingSet(player, moves, playerRack, playerTile, set, tilesSet);
                            }
                        }
                    }
                }
                else {
                    // run
                    if (tilesSet.size() < 13) {
                        if (playerTile.getCOLOUR().equals(tilesSet.get(0).getCOLOUR())) {
                            if (playerTile.getNUMBER() == tilesSet.get(0).getNUMBER() - 1 || playerTile.getNUMBER() == tilesSet.get(tilesSet.size() - 1).getNUMBER() + 1) {
                                // Create game state
                                createGameStateForExistingSet(player, moves, playerRack, playerTile, set, tilesSet);
                            }
                        }
                    }
                }
            }
        }

        // If the player cannot make a move, draw a tile from the pool (if possible)
        if (moves.size() == 0) {
            if (POOL.size() >= 1) {
                System.out.println("Player #" + player.getID() + " has drawn from the pool");
                List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

                List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
                newPlayerRack.add(newPool.remove(0));

                HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
                newRacks.replace(player, newPlayerRack);

                List<Set> newTable = new ArrayList<>(List.copyOf(TABLE));

                moves.add(new GameState(newRacks, newTable, newPool));
            }
        }

        return moves;
    }

    private void createGameStateForExistingSet(Player player, List<GameState> moves, List<Tile> playerRack, Tile playerTile, Set set, List<Tile> tilesSet) {
        List<Tile> newPlayerRack = new ArrayList<>(List.copyOf(playerRack));
        newPlayerRack.remove(playerTile);

        HashMap<Player, List<Tile>> newRacks = new HashMap<>(RACKS);
        newRacks.replace(player, newPlayerRack);

        List<Tile> newTilesSet = new ArrayList<>(List.copyOf(tilesSet));
        newTilesSet.add(playerTile);

        List<Set> newTable = new ArrayList<>(List.copyOf(TABLE));
        newTable.remove(set);
        for (Set set1 : Game.SETS) {
            if (set1.checkMatchingTiles(newTilesSet)) {
                newTable.add(set1);
                break;
            }
        }

        List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

        moves.add(new GameState(newRacks, newTable, newPool));
    }

    public void printRacks() {
        System.out.println("Racks:");
        for (Player player : RACKS.keySet()) {
            StringBuilder text = new StringBuilder();
            for (Tile tile : RACKS.get(player)) {
                if (!text.isEmpty()) {
                    text.append(" | ");
                }
                text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
            }
            System.out.println("* Player #" + player.getID() + ": " + text);
        }
    }

    public void printRack(Player player) {
        System.out.println("Rack:");
        StringBuilder text = new StringBuilder();
        for (Tile tile : RACKS.get(player)) {
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
        }
        System.out.println("* Player #" + player.getID() + ": " + text);
    }

    public void printTable() {
        System.out.println("Table:");
        for (Set set : TABLE) {
            StringBuilder text = new StringBuilder();
            for (Tile tile : set.getTILES()) {
                if (!text.isEmpty()) {
                    text.append(" | ");
                }
                text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
            }
            System.out.println("* Set #" + set.getID() + ": " + text);
        }
    }


}
