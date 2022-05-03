package Main;

import Players.Player;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GameState {

    private static long startTime = 0;
    private static final int maximumTime = 500;

    private String id;
    private int depth;
    private GameState parent;
    private final LinkedHashMap<Player, List<Tile>> RACKS;
    private final LinkedHashMap<Set, List<List<Tile>>> TABLE;
    private final List<Tile> POOL;

    public GameState(LinkedHashMap<Player, List<Tile>> racks, LinkedHashMap<Set, List<List<Tile>>>  table, List<Tile> pool) {
        this.depth = 1;
        this.parent = null;
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
        generateId();
    }

    public GameState(GameState parent, LinkedHashMap<Player, List<Tile>> racks, LinkedHashMap<Set, List<List<Tile>>>  table, List<Tile> pool) {
        this.id = "";
        this.depth = parent.depth + 1;
        this.parent = parent;
        this.RACKS = racks;
        this.TABLE = table;
        this.POOL = pool;
    }

    public String getId() {
        return id;
    }

    public int getDepth() {
        return depth;
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

    public void generateId() {
        StringBuilder id = new StringBuilder();

        for (Player player : RACKS.keySet()) {
            if (!id.isEmpty()) {
                id.append("-");
            }

            List<Tile> playerRack = new ArrayList<>(RACKS.get(player));
            StringBuilder playerRackText = new StringBuilder();

            while (!playerRack.isEmpty()) {
                if (!playerRackText.isEmpty()) {
                    playerRackText.append(",");
                }

                Tile tileWithSmallestId = null;
                int smallestId = Integer.MAX_VALUE;

                for (Tile tile : playerRack) {
                    int tileId = tile.getID();
                    if (tileId < smallestId) {
                        tileWithSmallestId = tile;
                        smallestId = tileId;
                    }
                }

                playerRack.remove(tileWithSmallestId);
                playerRackText.append(smallestId);
            }

            id.append(playerRackText);
        }

        id.append(":");

        List<Set> sets = new ArrayList<>(List.copyOf(TABLE.keySet().stream().toList()));
        StringBuilder setsText = new StringBuilder();
        while (!sets.isEmpty()) {
            if (!setsText.isEmpty()) {
                setsText.append("-");
            }

            Set setWithSmallestId = null;
            int smallestId = Integer.MAX_VALUE;

            for (Set set : sets) {
                int setId = set.getID();
                if (setId < smallestId) {
                    setWithSmallestId = set;
                    smallestId = setId;
                }
            }

            sets.remove(setWithSmallestId);
            setsText.append(smallestId).append(",").append(TABLE.get(setWithSmallestId).size());
        }

        id.append(setsText);

        this.id = id.toString();
    }

    public void setParent(GameState parent) {
        this.parent = parent;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<GameState> getMoves(Player player) {
        List<GameState> moves = new ArrayList<>();
        List<GameState> movesAndDummy = new ArrayList<>();

        startTime = System.currentTimeMillis();
        recursivelyEnumerateMoves(player, moves, movesAndDummy);

        // Filter out invalid states
        List<GameState> filteredList = new ArrayList<>();
        List<Tile> tilesOnCurrentTable = fetchTilesOnTable();
        for (GameState move : moves) {
            boolean valid = true;

            // Ensure that at least one tile is drawn from the player's rack
            if (move.RACKS.get(player).size() >= RACKS.get(player).size()) {
                valid = false;
            }

            if (valid) {
                // Ensure that no tile which is currently on the table has entered the player's rack
                List<Tile> tilesOnPotentialTable = move.fetchTilesOnTable();

                for (Tile tile : tilesOnCurrentTable) {
                    if (!tilesOnPotentialTable.contains(tile)) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    filteredList.add(move);
                }
            }
        }
        moves = filteredList;

        // If the player cannot make a move, draw a tile from the pool (if possible)
        if (moves.size() == 0) {
            if (POOL.size() >= 1) {
                GameState move = createChild();

                move.drawTileFromPool(player);

                moves.add(move);
            }
        }

        return moves;
    }

    public void drawTileFromPool(Player player) {
        Tile tileFromPool = POOL.remove(0);
        RACKS.get(player).add(tileFromPool);
        generateId();
        System.out.println("Draws {" + tileFromPool.getNUMBER() + ", " + tileFromPool.getCOLOUR() + "} from the pool");
    }

    private void recursivelyEnumerateMoves(Player player, List<GameState> moves, List<GameState> movesAndDummy) {
        if (depth < 50 && (System.currentTimeMillis() - startTime) < maximumTime) {
            List<Tile> playerRack = this.RACKS.get(player);

            // Draw set from rack on table
            for (Set set : Game.SETS) {
                List<Tile> tilesToRemove = set.drawableFromRack(playerRack);

                if (tilesToRemove.size() >= 3) {
                    // Perform move
                    GameState move = createChild();

                    move.RACKS.get(player).removeAll(tilesToRemove);
                    move.addSetInstance(set, tilesToRemove);

                    if (move.isWorthExploring(movesAndDummy)) {
                        moves.add(move);
                        move.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                    }
                }

                // Time limit check
                if (System.currentTimeMillis() - startTime >= maximumTime) {
                    return;
                }
            }

            // Merge two runs to make a new run
            for (Set set1 : TABLE.keySet()) {
                if (set1.getTYPE().equals("run")) {
                    for (Set set2 : TABLE.keySet()) {
                        if (set2.getTYPE().equals("run") && set1 != set2) {
                            List<Tile> set1Instance = TABLE.get(set1).get(0);
                            Tile set1FirstTile = set1Instance.get(0);
                            Tile set1LastTile = set1Instance.get(set1Instance.size() - 1);
                            List<Tile> set2Instance = TABLE.get(set2).get(0);
                            Tile set2FirstTile = set2Instance.get(0);
                            Tile set2LastTile = set2Instance.get(set2Instance.size() - 1);

                            if (set1FirstTile.getCOLOUR().equals(set2FirstTile.getCOLOUR())) {
                                if (set1LastTile.getNUMBER() == set2FirstTile.getNUMBER() - 1 ||
                                        set1FirstTile.getNUMBER() == set2LastTile.getNUMBER() + 1) {
                                    // Perform move
                                    GameState dummy = createChild();

                                    List<Tile> newSetInstance = new ArrayList<>();
                                    newSetInstance.addAll(set1Instance);
                                    newSetInstance.addAll(set2Instance);
                                    LinkedHashMap<Set, List<Tile>> setInstances = new LinkedHashMap<>();
                                    setInstances.put(set1, set1Instance);
                                    setInstances.put(set2, set2Instance);
                                    List<List<Tile>> newSetInstances = new ArrayList<>();
                                    newSetInstances.add(newSetInstance);
                                    dummy.replaceSetInstances(setInstances, newSetInstances);

                                    if (dummy.isWorthExploring(movesAndDummy)) {
                                        dummy.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                                    }
                                }
                            }
                        }

                        // Time limit check
                        if (System.currentTimeMillis() - startTime >= maximumTime) {
                            return;
                        }
                    }
                }

                // Time limit check
                if (System.currentTimeMillis() - startTime >= maximumTime) {
                    return;
                }
            }

            // Split a run into two new runs
            for (Set set : TABLE.keySet()) {
                if (set.getTYPE().equals("run") && set.getTILES().size() >= 6) {
                    // Perform move
                    GameState dummy = createChild();

                    List<Tile> setInstance = TABLE.get(set).get(0);
                    List<Tile> newSetInstance1 = new ArrayList<>();
                    List<Tile> newSetInstance2 = new ArrayList<>();
                    for (int i = 0; i < setInstance.size(); i++) {
                        if (i < 3) {
                            newSetInstance1.add(setInstance.get(i));
                        } else {
                            newSetInstance2.add(setInstance.get(i));
                        }
                    }
                    LinkedHashMap<Set, List<Tile>> setInstances = new LinkedHashMap<>();
                    setInstances.put(set, setInstance);
                    List<List<Tile>> newSetInstances = new ArrayList<>();
                    newSetInstances.add(newSetInstance1);
                    newSetInstances.add(newSetInstance2);
                    dummy.replaceSetInstances(setInstances, newSetInstances);

                    if (dummy.isWorthExploring(movesAndDummy)) {
                        dummy.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                    }
                }

                // Time limit check
                if (System.currentTimeMillis() - startTime >= maximumTime) {
                    return;
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
                        LinkedHashMap<Set, List<Tile>> setInstances = new LinkedHashMap<>();
                        setInstances.put(set, setInstance);
                        List<List<Tile>> newSetInstances = new ArrayList<>();
                        newSetInstances.add(newSetInstance);
                        move.replaceSetInstances(setInstances, newSetInstances);

                        if (move.isWorthExploring(movesAndDummy)) {
                            moves.add(move);
                            move.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                        }
                    }

                    // Time limit check
                    if (System.currentTimeMillis() - startTime >= maximumTime) {
                        return;
                    }
                }

                // Time limit check
                if (System.currentTimeMillis() - startTime >= maximumTime) {
                    return;
                }
            }

            // Remove a fourth tile from a group
            // Remove the first and last tile from a run
            for (Set set : TABLE.keySet()) {
                if (set.getTILES().size() >= 4) {
                    List<Tile> setInstance = TABLE.get(set).get(0);
                    List<Tile> tilesToRemove = new ArrayList<>();

                    if (set.getTYPE().equals("group")) {
                        tilesToRemove.addAll(setInstance);
                    } else {
                        // run
                        tilesToRemove.add(setInstance.get(0));
                        tilesToRemove.add(setInstance.get(setInstance.size() - 1));
                    }

                    for (Tile tile : tilesToRemove) {
                        // Perform move
                        GameState dummy = createChild();

                        dummy.RACKS.get(player).add(tile);
                        List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                        newSetInstance.remove(tile);
                        LinkedHashMap<Set, List<Tile>> setInstances = new LinkedHashMap<>();
                        setInstances.put(set, setInstance);
                        List<List<Tile>> newSetInstances = new ArrayList<>();
                        newSetInstances.add(newSetInstance);
                        dummy.replaceSetInstances(setInstances, newSetInstances);

                        if (dummy.isWorthExploring(movesAndDummy)) {
                            dummy.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                        }

                        // Time limit check
                        if (System.currentTimeMillis() - startTime >= maximumTime) {
                            return;
                        }
                    }
                }

                // Time limit check
                if (System.currentTimeMillis() - startTime >= maximumTime) {
                    return;
                }
            }
        }
    }

    public List<Tile> fetchTilesOnTable() {
        List<Tile> tilesOnTable = new ArrayList<>();

        for (Set set : TABLE.keySet()) {
            for (List<Tile> setInstance : TABLE.get(set)) {
                tilesOnTable.addAll(setInstance);
            }
        }

        return tilesOnTable;
    }

    public GameState createChild() {
        LinkedHashMap<Player, List<Tile>> newRacks = new LinkedHashMap<>(RACKS);
        for (Player player : newRacks.keySet()) {
            newRacks.replace(player, new ArrayList<>(List.copyOf(RACKS.get(player))));
        }

        LinkedHashMap<Set, List<List<Tile>>> newTable = new LinkedHashMap<>(TABLE);
        newTable.replaceAll((s, v) -> new ArrayList<>(List.copyOf(TABLE.get(s))));

        List<Tile> newPool = new ArrayList<>(List.copyOf(POOL));

        return new GameState(this, newRacks, newTable, newPool);
    }

    public void addSetInstance(Set set, List<Tile> setInstance) {
        if (!TABLE.containsKey(set)) {
            TABLE.put(set, new ArrayList<>());
        }
        TABLE.get(set).add(setInstance);
    }

    public void removeSetInstance(Set set, List<Tile> setInstance) {
        if (TABLE.get(set).size() == 1) {
            TABLE.remove(set);
        } else {
            TABLE.get(set).remove(setInstance);
        }
    }

    private void replaceSetInstances(LinkedHashMap<Set, List<Tile>> setInstances, List<List<Tile>> newSetInstances) {
        for (Set set : setInstances.keySet()) {
            removeSetInstance(set, setInstances.get(set));
        }

        for (Set set_ : Game.SETS) {
            for (List<Tile> newSetInstance : newSetInstances) {
                List<Tile> tilesInSet = set_.isExactMatch(newSetInstance);
                if (tilesInSet.size() > 0) {
                    addSetInstance(set_, tilesInSet);
                    newSetInstances.remove(newSetInstance);
                    break;
                }
            }

            if (newSetInstances.size() == 0) {
                break;
            }
        }
    }

    private boolean isWorthExploring(List<GameState> movesAndDummy) {
        generateId();

        for (GameState PREVIOUS_STATE : Game.PREVIOUS_STATES) {
            if (id.equals(PREVIOUS_STATE.id)) {
                // Duplicate
                return false;
            }
        }

        if (id.equals(Game.currentState.id)) {
            // Duplicate
            return false;
        }

        for (GameState state : movesAndDummy) {
            if (id.equals(state.id)) {
                // Duplicate
                if (depth >= state.depth) {
                    return false;
                }
                else {
                    // Duplicate at lower depth
                    movesAndDummy.remove(state);
                    break;
                }
            }
        }

        movesAndDummy.add(this);
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
            text.append(tile.getNUMBER()).append(", ").append(tile.getCOLOUR());
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
                    text.append(tile.getNUMBER()).append(", ").append(tile.getCOLOUR());
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
                System.out.print("- ");
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
                System.out.print("- ");
                newSet.print();
            }
        }

        // Print drawn tiles
        List<Tile> previousRack = parent.RACKS.get(player);
        List<Tile> currentRack = RACKS.get(player);
        List<Tile> drawnTiles = new ArrayList<>();
        for (Tile tile : previousRack) {
            if (!currentRack.contains(tile)) {
                drawnTiles.add(tile);
            }
        }

        if (drawnTiles.size() >= 1) {
            System.out.println("Drawn tiles:");
            while (!drawnTiles.isEmpty()) {
                Tile tileWithSmallestId = null;
                int smallestId = Integer.MAX_VALUE;

                for (Tile tile : drawnTiles) {
                    int id = tile.getID();
                    if (id < smallestId) {
                        tileWithSmallestId = tile;
                        smallestId = id;
                    }
                }

                drawnTiles.remove(tileWithSmallestId);
                System.out.print("- ");
                tileWithSmallestId.print();
            }
        }

        System.out.println("Rack:");
        printRack(player);
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
