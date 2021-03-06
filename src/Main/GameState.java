package Main;

import Players.Player;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GameState {

    private static long startTime;
    private static int maximumTime; // in ms
    private static int maximumDepth;

    private String id;
    private int depth;
    private double score; // determined by objective function
    private GameState parent;
    private final LinkedHashMap<Player, List<Tile>> racks;
    private final LinkedHashMap<Set, List<List<Tile>>> table;
    private final List<Tile> pool;

    public GameState(LinkedHashMap<Player, List<Tile>> racks, LinkedHashMap<Set, List<List<Tile>>>  table, List<Tile> pool) {
        this.depth = 0;
        this.score = 0;
        this.parent = null;
        this.racks = racks;
        this.table = table;
        this.pool = pool;
        generateId();
    }

    public GameState(GameState parent) {
        this.id = "";
        this.depth = parent.depth + 1;
        this.score = 0;
        this.parent = parent;
        this.racks = new LinkedHashMap<>(parent.racks);
        for (Player player : racks.keySet()) {
            racks.replace(player, new ArrayList<>(List.copyOf(racks.get(player))));
        }
        this.table = new LinkedHashMap<>(parent.table);
        table.replaceAll((s, v) -> new ArrayList<>(List.copyOf(table.get(s))));
        this.pool = new ArrayList<>(List.copyOf(parent.pool));
    }

    public String getId() {
        return id;
    }

    public int getDepth() {
        return depth;
    }

    public double getScore() {
        return score;
    }

    public GameState getParent() {
        return parent;
    }

    public LinkedHashMap<Player, List<Tile>> getRacks() {
        return racks;
    }

    public LinkedHashMap<Set, List<List<Tile>>> getTable() {
        return table;
    }

    public List<Tile> getPool() {
        return pool;
    }

    public void generateId() {
        StringBuilder id = new StringBuilder();

        for (Player player : racks.keySet()) {
            if (!id.isEmpty()) {
                id.append("-");
            }

            List<Tile> playerRack = new ArrayList<>(racks.get(player));
            StringBuilder playerRackText = new StringBuilder();

            while (!playerRack.isEmpty()) {
                if (!playerRackText.isEmpty()) {
                    playerRackText.append(",");
                }

                Tile tileWithSmallestId = null;
                int smallestId = Integer.MAX_VALUE;

                for (Tile tile : playerRack) {
                    int tileId = tile.getId();
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

        List<Set> sets = new ArrayList<>(List.copyOf(table.keySet().stream().toList()));
        StringBuilder setsText = new StringBuilder();
        while (!sets.isEmpty()) {
            if (!setsText.isEmpty()) {
                setsText.append("-");
            }

            Set setWithSmallestId = null;
            int smallestId = Integer.MAX_VALUE;

            for (Set set : sets) {
                int setId = set.getId();
                if (setId < smallestId) {
                    setWithSmallestId = set;
                    smallestId = setId;
                }
            }

            sets.remove(setWithSmallestId);
            setsText.append(smallestId).append(",").append(table.get(setWithSmallestId).size());
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

    public void setScore(double score) {
        this.score = score;
    }

    public void calculateScore(GameState currentState, Player player, String objectiveFunction) {
        List<Tile> currentPlayerRack = currentState.getRacks().get(player);
        List<Tile> potentialPlayerRack = racks.get(player);

        if (objectiveFunction.contains("ttc")) {
            // total tile count
            score = Math.max(0, currentPlayerRack.size() - potentialPlayerRack.size());
        } else if (objectiveFunction.contains("ttv")) {
            // total tile value
            for (Tile tile : currentPlayerRack) {
                if (!potentialPlayerRack.contains(tile)) {
                    score += tile.getNumber();
                }
            }
        } else {
            System.out.println("Error: invalid objective function \"" + objectiveFunction + "\"");
            System.exit(0);
        }

        if (objectiveFunction.contains("wscm")) {
            double M = 40;

            java.util.Set<Set> setsOldSolution = currentState.table.keySet();
            java.util.Set<Set> setsNewSolution = table.keySet();

            for (Set set : setsNewSolution) {
                if (setsOldSolution.contains(set)) {
                    double setCounterOldSolution = currentState.table.get(set).size();
                    double setCounterNewSolution = table.get(set).size();

                    score += Math.min(setCounterOldSolution, setCounterNewSolution) / M;
                }
            }
        }
    }

    public List<GameState> getMoves(Player player, Player playerScore, String objectiveFunction, int maxDepth, int maxTime) {
        List<GameState> moves = new ArrayList<>();
        List<GameState> movesAndDummy = new ArrayList<>();

        maximumDepth = maxDepth;
        maximumTime = maxTime;
        startTime = System.currentTimeMillis();
        recursivelyEnumerateMoves(player, moves, movesAndDummy);

        // Filter out invalid states
        List<GameState> filteredList = new ArrayList<>();
        List<Tile> tilesOnCurrentTable = fetchTilesOnTable();
        for (GameState move : moves) {
            // Ensure that at least one tile is drawn from the player's rack
            boolean valid = move.racks.get(player).size() < racks.get(player).size();

            if (valid) {
                // Ensure that no tile which is currently on the table has entered the player's rack
                List<Tile> tilesOnPotentialTable = move.fetchTilesOnTable();

                for (Tile tile1 : tilesOnCurrentTable) {
                    boolean matchFound = false;

                    for (Tile tile2 : tilesOnPotentialTable) {
                        if (tile1.isMatch(tile2)) {
                            matchFound = true;
                            break;
                        }
                    }

                    if (!matchFound) {
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

        // Calculate scores
        for (GameState move : moves) {
            move.calculateScore(this, playerScore, objectiveFunction);
        }

        return moves;
    }

    private void recursivelyEnumerateMoves(Player player, List<GameState> moves, List<GameState> movesAndDummy) {
        if (depth < maximumDepth && (System.currentTimeMillis() - startTime) < maximumTime) {
            List<Tile> playerRack = this.racks.get(player);

            // Replace joker tiles on table with equivalent tiles from rack
            for (Set set : table.keySet()) {
                List<Tile> setInstance = table.get(set).get(0);

                for (int i = 0; i < setInstance.size(); i++) {
                    Tile tile = setInstance.get(i);

                    if (tile.getColour().equals("joker")) {
                        int tileNumber = 0;
                        List<String> tileColours = new ArrayList<>();

                        if (set.getType().equals("group")) {
                            tileColours.add("black");
                            tileColours.add("red");
                            tileColours.add("orange");
                            tileColours.add("blue");

                            for (Tile tile_ : setInstance) {
                                String colour = tile_.getColour();

                                if (!colour.equals("joker")) {
                                    tileNumber = tile_.getNumber();
                                    tileColours.removeIf(tileColour -> tileColour.equals(colour));
                                }
                            }
                        } else {
                            // run
                            if (i == 0) {
                                Tile nextTile = setInstance.get(1);
                                tileNumber = nextTile.getNumber() - 1;
                                tileColours.add(nextTile.getColour());
                            } else {
                                Tile previousTile = setInstance.get(i - 1);
                                tileNumber = previousTile.getNumber() + 1;
                                tileColours.add(previousTile.getColour());
                            }
                        }

                        // Find equivalent tile in rack
                        for (Tile tile_ : playerRack) {
                            for (String tileColour : tileColours) {
                                if (tile_.getColour().equals(tileColour) && tile_.getNumber() == tileNumber) {
                                    // Perform move
                                    GameState dummy = new GameState(this);

                                    List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                                    newSetInstance.remove(tile);
                                    newSetInstance.add(tile_);

                                    dummy.racks.get(player).remove(tile_);
                                    dummy.racks.get(player).add(tile);
                                    dummy.removeSetInstance(set, setInstance);
                                    dummy.findAndAddCorrespondingSet(newSetInstance);

                                    if (dummy.isWorthExploring(movesAndDummy)) {
                                        dummy.recursivelyEnumerateMoves(player, moves, movesAndDummy);
                                    }
                                }
                            }
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

            // Draw set from rack on table
            for (Set set : Game.sets) {
                List<Tile> setInstance = set.drawableFromRack(playerRack);

                if (!setInstance.isEmpty()) {
                    // Perform move
                    GameState move = new GameState(this);

                    move.racks.get(player).removeAll(setInstance);
                    move.addSetInstance(set, setInstance);

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

            // Add one tile from rack to existing group
            // Add one tile from rack to front or back of existing run
            for (Tile playerTile : playerRack) {
                for (Set set : table.keySet()) {
                    if (set.isExpandingTile(playerTile)) {
                        // Perform move
                        GameState move = new GameState(this);

                        List<Tile> setInstance = table.get(set).get(0);
                        List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                        newSetInstance.add(playerTile);

                        move.racks.get(player).remove(playerTile);
                        move.removeSetInstance(set, setInstance);
                        move.findAndAddCorrespondingSet(newSetInstance);

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
            for (Set set : table.keySet()) {
                if (set.getTiles().size() > 3) {
                    List<Tile> setInstance = table.get(set).get(0);
                    List<Tile> tilesToRemove = new ArrayList<>();

                    if (set.getType().equals("group")) {
                        tilesToRemove.addAll(setInstance);
                    } else {
                        // run
                        tilesToRemove.add(setInstance.get(0));
                        tilesToRemove.add(setInstance.get(setInstance.size() - 1));
                    }

                    for (Tile tile : tilesToRemove) {
                        // Perform move
                        GameState dummy = new GameState(this);

                        List<Tile> newSetInstance = new ArrayList<>(List.copyOf(setInstance));
                        newSetInstance.remove(tile);

                        dummy.racks.get(player).add(tile);
                        dummy.removeSetInstance(set, setInstance);
                        dummy.findAndAddCorrespondingSet(newSetInstance);

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

        for (Set set : table.keySet()) {
            for (List<Tile> setInstance : table.get(set)) {
                tilesOnTable.addAll(setInstance);
            }
        }

        return tilesOnTable;
    }

    public void addSetInstance(Set set, List<Tile> setInstance) {
        if (!table.containsKey(set)) {
            table.put(set, new ArrayList<>());
        }
        table.get(set).add(setInstance);
    }

    public void removeSetInstance(Set set, List<Tile> setInstance) {
        if (table.get(set).size() == 1) {
            table.remove(set);
        } else {
            table.get(set).remove(setInstance);
        }
    }

    private void findAndAddCorrespondingSet(List<Tile> setInstance) {
        for (Set set : Game.sets) {
            List<Tile> sortedSetInstance = set.isExactMatch(setInstance);

            if (!sortedSetInstance.isEmpty()) {
                addSetInstance(set, sortedSetInstance);
                break;
            }
        }
    }

    private boolean isWorthExploring(List<GameState> movesAndDummy) {
        generateId();

        for (GameState previousState : Game.previousStates) {
            if (id.equals(previousState.id)) {
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

    public void drawTileFromPool(Player player) {
        Tile tileFromPool = pool.remove(0);
        racks.get(player).add(tileFromPool);
        generateId();
        System.out.println("Draws {" + tileFromPool.getNumber() + ", " + tileFromPool.getColour() + "} from the pool");
    }

    public void printRacks() {
        for (Player player : racks.keySet()) {
            System.out.print("* Player #" + player.getId() + "'s rack: ");
            printRack(player);
        }
    }

    public void printRack(Player player) {
        StringBuilder text = new StringBuilder();
        for (Tile tile : racks.get(player)) {
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(tile.getNumber()).append(", ").append(tile.getColour());
        }
        System.out.println(text);
    }

    public void printTable() {
        System.out.println("Table:");
        for (Set set : table.keySet()) {
            for (List<Tile> instanceOnTable : table.get(set)) {
                StringBuilder text = new StringBuilder();
                for (Tile tile : instanceOnTable) {
                    if (!text.isEmpty()) {
                        text.append(" | ");
                    }
                    text.append(tile.getNumber()).append(", ").append(tile.getColour());
                }
                System.out.println("* Set #" + set.getId() + ": " + text);
            }
        }
    }

    public void printMoveInfo(Player player) {
        // Print removed sets
        List<Set> removedSets = new ArrayList<>();
        for (Set setParent : parent.table.keySet()) {
            int timesRemoved = parent.table.get(setParent).size();

            if (table.containsKey(setParent)) {
                int instanceAmount = table.get(setParent).size();
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
        for (Set set : table.keySet()) {
            int timesAdded = table.get(set).size();

            if (parent.table.containsKey(set)) {
                int instanceAmount = parent.table.get(set).size();
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
        List<Tile> previousRack = parent.racks.get(player);
        List<Tile> currentRack = racks.get(player);
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
                    int id = tile.getId();
                    if (id < smallestId) {
                        tileWithSmallestId = tile;
                        smallestId = id;
                    }
                }

                drawnTiles.remove(tileWithSmallestId);
                System.out.print("- ");
                assert tileWithSmallestId != null;
                tileWithSmallestId.print();
            }
        }

        System.out.println("Rack:");
        printRack(player);
    }

    public void visualize() {
        // Reset board
        for (Tile tile : Game.tiles) {
            tile.getImage().setVisible(false);
        }

        // Racks
        for (Player player : racks.keySet()) {
            List<Tile> rack = racks.get(player);
            List<double[]> coordinates = Main.coordinatesRacks.get(player.getId());
            for (int i = 0; i < rack.size(); i++) {
                rack.get(i).setImageCoordinates(coordinates.get(i));
            }
        }

        // Sets
        int instanceCounter = 0;
        for (Set set : table.keySet()) {
            for (List<Tile> instanceOnTable : table.get(set)) {
                List<double[]> coordinates = Main.coordinatesTable.get(instanceCounter);

                for (int tile = 0; tile < instanceOnTable.size(); tile++) {
                    instanceOnTable.get(tile).setImageCoordinates(coordinates.get(tile));
                }

                instanceCounter++;
            }
        }

        // Pool counter
        Text counter = (Text) Game.nodes.get(0);
        counter.setText(String.valueOf(pool.size()));
    }

}
