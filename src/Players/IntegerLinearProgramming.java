package Players;

import Main.Game;
import Main.GameState;
import Main.Set;
import Main.Tile;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class IntegerLinearProgramming implements Player {

    private static final LinkedHashMap<Integer, List<Tile>> tileTypes = new LinkedHashMap<>(); // Maps index i to type of tile
    private static final LinkedHashMap<Integer, Set> sets = new LinkedHashMap<>(); // Maps index j to set
    private static boolean nativeLibrariesLoaded = false;
    private static final boolean debug = false;

    private final int ID;
    private final String objectiveFunction;
    private boolean stuck;

    public IntegerLinearProgramming(int id, String objectiveFunction) {
        this.ID = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;

        if (tileTypes.isEmpty()) {
            for (int i = 0; i < Game.TILES.size(); i+=2) {
                List<Tile> tiles = new ArrayList<>();
                tiles.add(Game.TILES.get(i));
                tiles.add(Game.TILES.get(i + 1));
                tileTypes.put(tileTypes.size() + 1, tiles);
            }

            for (int i = 0; i < Game.SETS.size(); i++) {
                sets.put(sets.size() + 1, Game.SETS.get(i));
            }
        }

        if (!nativeLibrariesLoaded) {
            Loader.loadNativeLibraries();
            nativeLibrariesLoaded = true;
        }
    }

    public String getName() {
        return "ILP";
    }

    public int getID() {
        return ID;
    }

    public String getObjectiveFunction() {
        return objectiveFunction;
    }

    public boolean isStuck() {
        return stuck;
    }

    public void unstuck() {
        this.stuck = false;
    }

    public GameState makeMove(GameState currentState) {
        // Create the linear solver with the SCIP backend.
        MPSolver solver = MPSolver.createSolver("SCIP");
        List<Integer> sValues = new ArrayList<>();
        List<Integer> tValues = new ArrayList<>();
        List<Integer> rValues = new ArrayList<>();
        List<Integer> vValues = new ArrayList<>();
        List<MPVariable> xValues = new ArrayList<>();
        List<MPVariable> yValues = new ArrayList<>();

        // x_j (v): set j can be placed 0, 1 or 2 times onto the table
        for (Integer j : sets.keySet()) {
            MPVariable x_j = solver.makeIntVar(0, 2, "x_" + j);
            xValues.add(x_j);
        }

        // t_i (p): tile i is 0, 1 or 2 times on the table
        // r_i (p): tile i is 0, 1 or 2 times on the player's rack
        // v_i (p): value of tile i
        // y_i (v): tile i can be placed 0, 1 or 2 times from the player's rack onto the table
        // s_ij (p): indicates whether tile i is in set j (yes = 1, no = 0)
        List<Tile> tilesOnTable = currentState.fetchTilesOnTable();
        List<Tile> tilesOnRack = new ArrayList<>(List.copyOf(currentState.getRACKS().get(this)));
        for (Integer i : tileTypes.keySet()) {
            List<Tile> copies = tileTypes.get(i);
            int t_i = 0;
            int r_i = 0;

            for (Tile copy : copies) {
                if (tilesOnTable.contains(copy)) {
                    t_i++;
                    tilesOnTable.remove(copy);
                }
                else if (tilesOnRack.contains(copy)) {
                    r_i++;
                    tilesOnRack.remove(copy);
                }
            }

            MPVariable y_i = solver.makeIntVar(0, 2, "y_" + i);
            MPConstraint constraint1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, r_i, "constraint y_" + i);
            constraint1.setCoefficient(y_i, 1);

            MPConstraint constraint2 = solver.makeConstraint(t_i, t_i, "constraint sum_" + i);
            for (Integer j : sets.keySet()) {
                Set set = sets.get(j);
                int s_ij = 0;

                if (copies.get(0).getSETS().contains(set)) {
                    s_ij = 1;
                    constraint2.setCoefficient(xValues.get(j - 1), 1);
                }

                sValues.add(s_ij);
            }
            constraint2.setCoefficient(y_i, -1);

            tValues.add(t_i);
            rValues.add(r_i);
            vValues.add(copies.get(0).getNUMBER());
            yValues.add(y_i);
        }

        // Objective function: maximize the number of tiles to draw from the player's rack onto the table
        MPObjective objective = solver.objective();
        for (int a = 0; a < yValues.size(); a++) {
            MPVariable y_i = yValues.get(a);
            int coefficient = 0;
            if (objectiveFunction.equals("ttc")) {
                coefficient = 1;
            }
            else if (objectiveFunction.equals("ttv")) {
                coefficient = vValues.get(a);
            }
            else {
                System.out.println("Error: invalid objective function \"" + objectiveFunction + "\"");
                System.exit(0);
            }
            objective.setCoefficient(y_i, coefficient);
        }
        objective.setMaximization();

        // Debugging
        if (debug) {
            System.out.println("<<< DEBUG START >>>");
            System.out.println("Number of tile types = " + tileTypes.size());
            System.out.println("Number of sets = " + sets.size());
            System.out.println("Number of s_ij parameters = " + sValues.size());
            System.out.println("Number of t_i parameters = " + tValues.size());
            System.out.println("Number of r_i parameters = " + rValues.size());
            System.out.println("Number of v_i parameters = " + vValues.size());
            System.out.println("Number of x_j variables = " + xValues.size());
            System.out.println("Number of y_i variables = " + yValues.size() + "\n");
        }

        // Solve
        solver.solve();
        if (debug) {
            System.out.println("Solution:");
            System.out.println("- Objective value = " + objective.value() + "\n");
        }

        // Convert to GameState
        GameState newState = currentState.createChild();
        tilesOnRack = newState.getRACKS().get(this);
        List<Tile> drawnTiles = new ArrayList<>();
        for (Integer i : tileTypes.keySet()) {
            List<Tile> copies = tileTypes.get(i);
            int y_i = (int) yValues.get(i - 1).solutionValue();

            for (int a = 0; a < y_i; a++) {
                for (Tile copy : copies) {
                    if (tilesOnRack.contains(copy)) {
                        tilesOnRack.remove(copy);
                        drawnTiles.add(copy);
                        break;
                    }
                }

                if (debug) {
                    System.out.print("- Draws ");
                    tileTypes.get(i).get(0).print();
                }
            }
        }

        if (debug) {
            System.out.println();
        }

        if (!drawnTiles.isEmpty()) {
            List<Set> drawnSets = new ArrayList<>();
            for (Integer j : sets.keySet()) {
                Set set = sets.get(j);
                int differenceInNumber = (int) xValues.get(j - 1).solutionValue(); // x_j

                if (currentState.getTABLE().containsKey(set)) {
                    differenceInNumber -= currentState.getTABLE().get(set).size();
                }

                if (differenceInNumber >= 1) {
                    for (int a = 0; a < differenceInNumber; a++) {
                        drawnSets.add(set);

                        if (debug) {
                            System.out.print("- Draws ");
                            set.print();
                        }
                    }
                } else if (differenceInNumber <= -1) {
                    differenceInNumber = differenceInNumber * -1;
                    for (int a = 0; a < differenceInNumber; a++) {
                        List<Tile> setInstance = newState.getTABLE().get(set).get(0);
                        drawnTiles.addAll(setInstance);
                        newState.removeSetInstance(set, setInstance);

                        if (debug) {
                            System.out.print("- Removed ");
                            set.print();
                        }
                    }
                }
            }

            if (debug) {
                System.out.println("<<< DEBUG END >>>");
            }

            // Process changes in new GameState
            for (Set set : drawnSets) {
                List<Tile> setInstance = set.drawableFromRack(drawnTiles);
                drawnTiles.removeAll(setInstance);
                newState.addSetInstance(set, setInstance);
            }

            newState.calculateScore(currentState, this);
            return newState;
        }
        else {
            if (debug) {
                System.out.println("<<< DEBUG END >>>");
            }

            // Draw a tile from the pool (if possible)
            if (currentState.getPOOL().size() >= 1) {
                newState.drawTileFromPool(this);
                return newState;
            }
            else {
                System.out.println("Player #" + ID + " is unable to make a move\n");
                this.stuck = true;
                return currentState;
            }
        }
    }

    public boolean checkWin(GameState state) {
        return state.getRACKS().get(this).size() == 0;
    }

}
