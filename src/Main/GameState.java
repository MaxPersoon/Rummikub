package Main;

import Players.Player;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GameState {

    private GameState parent;
    private final LinkedHashMap<Player, List<Tile>> RACKS;
    private final LinkedHashMap<Set, List<List<Tile>>> TABLE;
    private final List<Tile> POOL;

    public GameState(GameState parent, LinkedHashMap<Player, List<Tile>> racks, LinkedHashMap<Set, List<List<Tile>>>  table, List<Tile> pool) {
        this.parent = parent;
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
    }

    public GameState getParent() {
        return parent;
    }

    public LinkedHashMap<Player, List<Tile>> getRACKS() {
        return RACKS;
    }

    public LinkedHashMap<Set, List<List<Tile>>> getTABLE() {
        return TABLE;
    }

    public List<Tile> getPOOL() {
        return POOL;
    }

    public void setParent(GameState parent) {
        this.parent = parent;
    }

    public List<GameState> getMoves(Player player) {
        List<GameState> allMoves = new ArrayList<>();

        recursivelyEnumerateMoves(player, allMoves);

        // If the player cannot make a move, draw a tile from the pool (if possible)
        if (allMoves.size() == 0) {
            if (POOL.size() >= 1) {
                GameState move = createChild();

                Tile tileFromPool = move.POOL.remove(0);
                move.RACKS.get(player).add(tileFromPool);
                System.out.println("Draws {" + tileFromPool.getNUMBER() + ", " + tileFromPool.getCOLOUR() + "} from the pool");

                allMoves.add(move);
            }
        }

        return allMoves;
    }

    public void recursivelyEnumerateMoves(Player player, List<GameState> allMoves) {
        List<Tile> playerRack = this.RACKS.get(player);

        // Draw set from rack on table
        for (Set set : Game.SETS) {
            List<Tile> tilesToRemove = set.drawableFromRack(playerRack);

            if (tilesToRemove.size() >= 3) {
                // Perform move
                GameState move = createChild();

                move.RACKS.get(player).removeAll(tilesToRemove);
                move.addSetInstance(set, tilesToRemove);

                if (!move.checkForDuplicates(allMoves)) {
                    allMoves.add(move);
                    move.recursivelyEnumerateMoves(player, allMoves);
                }
            }
        }

        // Add one tile from rack to existing group
        // Add one tile from rack to front or back of existing run
        for (Tile playerTile : playerRack) {
            for (Set set : TABLE.keySet()) {
                if (set.isExpandingTile(playerTile)) {
                    // Perform move
                    GameState move = createChild();

                    move.RACKS.get(player).remove(playerTile);
                    List<Tile> setInstance = TABLE.get(set).get(0);
                    List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                    newSetInstance.add(playerTile);
                    move.replaceSetInstance(set, setInstance, newSetInstance);

                    if (!move.checkForDuplicates(allMoves)) {
                        allMoves.add(move);
                        move.recursivelyEnumerateMoves(player, allMoves);
                    }
                }
            }
        }

        // Remove a fourth tile from a group and use it to form a new set
        // Remove the first and last tile from a run and use it to form a new set
        for (Set set : TABLE.keySet()) {
            if (set.getTILES().size() >= 4) {
                List<Tile> setInstance = TABLE.get(set).get(0);
                List<Tile> tilesToRemove = new ArrayList<>();

                if (set.getTYPE().equals("group")) {
                    tilesToRemove.addAll(setInstance);
                }
                else {
                    // run
                    tilesToRemove.add(setInstance.get(0));
                    tilesToRemove.add(setInstance.get(setInstance.size() - 1));
                }

                for (Tile tile : tilesToRemove) {
                    // Perform move
                    GameState move = createChild();

                    move.RACKS.get(player).add(tile);
                    List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                    newSetInstance.remove(tile);
                    move.replaceSetInstance(set, setInstance, newSetInstance);

                    if (!move.checkForDuplicates(allMoves)) {
                        move.recursivelyEnumerateMoves(player, allMoves);
                    }
                }
            }
        }
    }

    private GameState createChild() {
        LinkedHashMap<Player, List<Tile>> newRacks = new LinkedHashMap<>(RACKS);
        for (Player player : newRacks.keySet()) {
            newRacks.replace(player, new ArrayList<>(List.copyOf(RACKS.get(player))));
        }

        LinkedHashMap<Set, List<List<Tile>>> newTable = new LinkedHashMap<>(TABLE);
        newTable.replaceAll((s, v) -> new ArrayList<>(List.copyOf(TABLE.get(s))));

        List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

        return new GameState(this, newRacks, newTable, newPool);
    }

    private void addSetInstance(Set set, List<Tile> setInstance) {
        if (!TABLE.containsKey(set)) {
            TABLE.put(set, new ArrayList<>());
        }
        TABLE.get(set).add(setInstance);
    }

    private void removeSetInstance(Set set, List<Tile> setInstance) {
        if (TABLE.get(set).size() == 1) {
            TABLE.remove(set);
        } else {
            TABLE.get(set).remove(setInstance);
        }
    }

    private void replaceSetInstance(Set set, List<Tile> setInstance, List<Tile> newSetInstance) {
        removeSetInstance(set, setInstance);

        for (Set set_ : Game.SETS) {
            List<Tile> tilesInSet = set_.isExactMatch(newSetInstance);
            if (tilesInSet.size() > 0) {
                addSetInstance(set_, tilesInSet);
                break;
            }
        }
    }

    public boolean checkForDuplicates(List<GameState> moves) {
        for (GameState PREVIOUS_STATE : Game.PREVIOUS_STATES) {
            if (isDuplicate(PREVIOUS_STATE)) {
                return true;
            }
        }

        if (isDuplicate(Game.currentState)) {
            return true;
        }

        for (GameState move : moves) {
            if (isDuplicate(move)) {
                return true;
            }
        }

        return false;
    }

    private boolean isDuplicate(GameState stateToCheck) {
        for (Player player : RACKS.keySet()) {
            List<Tile> playerRack = RACKS.get(player);
            List<Tile> playerRackToCheck = stateToCheck.RACKS.get(player);
            if (playerRack.size() != playerRackToCheck.size()) {
                return false;
            }
            for (Tile playerTile : playerRack) {
                boolean matchFound = false;

                for (Tile playerTileToCheck : playerRackToCheck) {
                    if (playerTile.isMatch(playerTileToCheck)) {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    return false;
                }
            }
        }

        List<Set> sets = TABLE.keySet().stream().toList();
        List<Set> setsToCheck = stateToCheck.TABLE.keySet().stream().toList();
        if (sets.size() != setsToCheck.size()) {
            return false;
        }
        for (Set set : sets) {
            if (!setsToCheck.contains(set)) {
                return false;
            }
            if (TABLE.get(set).size() != stateToCheck.TABLE.get(set).size()) {
                return false;
            }
        }

        return true;
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
            for (List<Tile> instanceOnTable : TABLE.get(set)) {
                StringBuilder text = new StringBuilder();
                for (Tile tile : instanceOnTable) {
                    if (!text.isEmpty()) {
                        text.append(" | ");
                    }
                    text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
                }
                System.out.println("* Set #" + set.getID() + ": " + text);
            }
        }
    }

    public void printMoveInfo(Player player) {
        // Print removed sets
        List<Set> removedSets = new ArrayList<>();
        for (Set setParent : parent.TABLE.keySet()) {
            int timesRemoved = parent.TABLE.get(setParent).size();

            if (TABLE.containsKey(setParent)) {
                int instanceAmount = TABLE.get(setParent).size();
                timesRemoved -= instanceAmount;
            }

            if (timesRemoved >= 1) {
                for (int i = 0; i < timesRemoved; i++) {
                    removedSets.add(setParent);
                }
            }
        }

        if (removedSets.size() >= 1) {
            System.out.println("Removed sets:");
            for (Set removedSet : removedSets) {
                removedSet.print();
            }
        }

        // Print new sets
        List<Set> newSets = new ArrayList<>();
        for (Set set : TABLE.keySet()) {
            int timesAdded = TABLE.get(set).size();

            if (parent.TABLE.containsKey(set)) {
                int instanceAmount = parent.TABLE.get(set).size();
                timesAdded -= instanceAmount;
            }

            if (timesAdded >= 1) {
                for (int i = 0; i < timesAdded; i++) {
                    newSets.add(set);
                }
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
        int instanceCounter = 0;
        for (Set set : TABLE.keySet()) {
            for (List<Tile> instanceOnTable : TABLE.get(set)) {
                List<double[]> coordinates = Main.COORDINATES_TABLE.get(instanceCounter);

                for (int tile = 0; tile < instanceOnTable.size(); tile++) {
                    instanceOnTable.get(tile).setImageCoordinates(coordinates.get(tile));
                }

                instanceCounter++;
            }
        }

        // Pool counter
        Text counter = (Text) Game.nodes.get(0);
        counter.setText(String.valueOf(POOL.size()));
    }

}
