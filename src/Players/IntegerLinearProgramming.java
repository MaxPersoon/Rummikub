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

    public static final LinkedHashMap<Integer, List<Tile>> tileTypes = new LinkedHashMap<>(); // Maps index i to type of tile
    public static final LinkedHashMap<Integer, Set> sets = new LinkedHashMap<>(); // Maps index j to set
    private static boolean nativeLibrariesLoaded = false;
    private static final boolean debug = false;

    private final int id;
    private final String objectiveFunction;
    private boolean stuck;
    private List<MPVariable> xValues;
    private List<MPVariable> yValues;

    public IntegerLinearProgramming(int id, String objectiveFunction) {
        this.id = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;

        if (tileTypes.isEmpty()) {
            for (int i = 0; i < Game.tiles.size(); i+=2) {
                List<Tile> tiles = new ArrayList<>();
                tiles.add(Game.tiles.get(i));
                tiles.add(Game.tiles.get(i + 1));
                tileTypes.put(tileTypes.size() + 1, tiles);
            }

            for (int i = 0; i < Game.sets.size(); i++) {
                sets.put(sets.size() + 1, Game.sets.get(i));
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

    public int getId() {
        return id;
    }

    public String getObjectiveFunction() {
        return objectiveFunction;
    }

    public boolean isStuck() {
        return stuck;
    }

    public void stuck() {
        this.stuck = true;
    }

    public void unstuck() {
        this.stuck = false;
    }

    public GameState makeMove(GameState currentState) {
        double objectiveValue = createModelAndSolve(currentState);
        return translateSolutionToState(currentState, objectiveValue);
    }

    private double createModelAndSolve(GameState currentState) {
        // Create the linear solver with the SCIP backend
        MPSolver solver = MPSolver.createSolver("SCIP");
        List<Integer> sValues = new ArrayList<>();
        List<Integer> wValues = new ArrayList<>();
        List<Integer> tValues = new ArrayList<>();
        List<Integer> rValues = new ArrayList<>();
        List<Integer> vValues = new ArrayList<>();
        xValues = new ArrayList<>();
        List<MPVariable> zValues = new ArrayList<>();
        yValues = new ArrayList<>();

        // s_ij (p): tile i occurs 0, 1 or 2 times in set j (non-joker: maximum of 1)
        // w_j (p): set j is 0, 1 or 2 times on the table
        // t_i (p): tile i is 0, 1 or 2 times on the table
        // r_i (p): tile i is 0, 1 or 2 times on the player's rack
        // v_i (p): value of tile i
        // x_j (v): set j can be placed 0, 1 or 2 times onto the table
        // z_j (v): set j occurs 0, 1 or 2 times in the old and in the new solutions
        // y_i (v): tile i can be placed 0, 1 or 2 times from the player's rack onto the table

        // Extract parameter values; create variables and constraints
        for (Integer j : sets.keySet()) {
            MPVariable x_j = solver.makeIntVar(0, 2, "x_" + j);
            xValues.add(x_j);

            if (objectiveFunction.contains("wscm")) {
                int w_j = 0;

                Set set = sets.get(j);
                LinkedHashMap<Set, List<List<Tile>>> table = currentState.getTable();
                if (table.containsKey(set)) {
                    w_j = table.get(set).size();
                }

                MPVariable z_j = solver.makeIntVar(0, 2, "z_" + j);
                MPConstraint constraint1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "constraint #1 z_" + j);
                constraint1.setCoefficient(z_j, 1);
                constraint1.setCoefficient(x_j, -1);
                MPConstraint constraint2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, w_j, "constraint #2 z_" + j);
                constraint2.setCoefficient(z_j, 1);

                wValues.add(w_j);
                zValues.add(z_j);
            }
        }

        List<Tile> tilesOnTable = currentState.fetchTilesOnTable();
        List<Tile> tilesOnRack = new ArrayList<>(List.copyOf(currentState.getRacks().get(this)));
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

                if (i == 53) {
                    // Joker
                    for (List<Tile> copies_ : set.getTiles()) {
                        Tile copy = copies_.get(0);
                        if (copy.getColour().equals("joker")) {
                            s_ij++;
                        }
                    }
                } else {
                    // Non-joker
                    if (copies.get(0).getSets().contains(set)) {
                        s_ij = 1;
                    }
                }

                if (s_ij >= 1) {
                    constraint2.setCoefficient(xValues.get(j - 1), s_ij);
                }
                sValues.add(s_ij);
            }
            constraint2.setCoefficient(y_i, -1);

            tValues.add(t_i);
            rValues.add(r_i);
            yValues.add(y_i);

            if (objectiveFunction.contains("ttv")) {
                vValues.add(copies.get(0).getNumber());
            }
        }

        // Create objective function
        MPObjective objective = solver.objective();
        objective.setMaximization();

        if (objectiveFunction.contains("ttc")) {
            for (MPVariable y_i : yValues) {
                objective.setCoefficient(y_i, 1);
            }
        } else if (objectiveFunction.contains("ttv")) {
            for (int a = 0; a < yValues.size(); a++) {
                objective.setCoefficient(yValues.get(a), vValues.get(a));
            }
        } else {
            System.out.println("Error: invalid objective function \"" + objectiveFunction + "\"");
            System.exit(0);
        }

        if (objectiveFunction.contains("wscm")) {
            double M = 40;
            for (MPVariable z_j : zValues) {
                objective.setCoefficient(z_j, 1.0 / M);
            }
        }

        // Solve
        solver.solve();
        double objectiveValue = objective.value();

        // Debugging
        if (debug) {
            System.out.println("<<< DEBUG START >>>");
            System.out.println("Number of tile types = " + tileTypes.size());
            System.out.println("Number of sets = " + sets.size());
            System.out.println("Number of s_ij parameters = " + sValues.size());
            System.out.println("Number of t_i parameters = " + tValues.size());
            System.out.println("Number of r_i parameters = " + rValues.size());
            System.out.println("Number of x_j variables = " + xValues.size());
            System.out.println("Number of y_i variables = " + yValues.size());

            if (objectiveFunction.contains("ttv")) {
                System.out.println("Number of v_i parameters = " + vValues.size());
            }

            if (objectiveFunction.contains("wscm")) {
                System.out.println("Number of w_j parameters = " + wValues.size());
                System.out.println("Number of z_j variables = " + zValues.size());
            }

            System.out.println();

            System.out.println("Solution:");
            System.out.println("- Objective value = " + objectiveValue + "\n");
        }

        return objectiveValue;
    }

    private GameState translateSolutionToState(GameState currentState, double objectiveValue) {
        // Determine what tiles have been drawn from this player's rack
        GameState newState = new GameState(currentState);
        List<Tile> tilesOnRack = newState.getRacks().get(this);
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
            // Determine what sets have been drawn and what sets have been removed
            List<Set> drawnSets = new ArrayList<>();
            for (Integer j : sets.keySet()) {
                Set set = sets.get(j);
                int differenceInNumber = (int) xValues.get(j - 1).solutionValue(); // x_j

                if (currentState.getTable().containsKey(set)) {
                    differenceInNumber -= currentState.getTable().get(set).size();
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
                        List<Tile> setInstance = newState.getTable().get(set).get(0);
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

            newState.setScore(objectiveValue);
            return newState;
        }
        else {
            if (debug) {
                System.out.println("<<< DEBUG END >>>");
            }

            return currentState;
        }
    }

    public boolean checkWin(GameState state) {
        return state.getRacks().get(this).size() == 0;
    }

}
