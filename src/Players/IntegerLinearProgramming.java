package Players;

import Main.Game;
import Main.GameState;
import Main.Set;
import Main.Tile;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntegerLinearProgramming implements Player {

    private static final HashMap<Integer, List<Tile>> tileTypes = new HashMap<>(); // Maps index i to type of tile
    private static final HashMap<Integer, Set> sets = new HashMap<>(); // Maps index j to set
    private static boolean nativeLibrariesLoaded = false;

    private final int ID;
    private boolean stuck;

    public IntegerLinearProgramming(int id) {
        this.ID = id;
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

    public int getID() {
        return ID;
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
        List<MPVariable> sValues = new ArrayList<>();
        List<MPVariable> tValues = new ArrayList<>();
        List<MPVariable> rValues = new ArrayList<>();
        List<MPVariable> xValues = new ArrayList<>();
        List<MPVariable> yValues = new ArrayList<>();

        // s_ij (p): indicates whether tile i is in set j (yes = 1, no = 0)
        // x_j (v): set j can be placed 0, 1 or 2 times onto the table
        for (Integer j : sets.keySet()) {
            Set set = sets.get(j);

            for (Integer i : tileTypes.keySet()) {
                Tile tileType = tileTypes.get(i).get(0);

                MPVariable s_ij;
                String name = "s_" + i.toString() + j.toString();
                if (tileType.getSETS().contains(set)) {
                    s_ij = solver.makeIntVar(1, 1, name);
                }
                else {
                    s_ij = solver.makeIntVar(0, 0, name);
                }

                sValues.add(s_ij);
            }

            MPVariable x_j = solver.makeIntVar(0, 2, "x_" + j);
            xValues.add(x_j);
        }

        // t_i (p): tile i is 0, 1 or 2 times on the table
        // r_i (p): tile i is 0, 1 or 2 times on the player's rack
        // y_i (v): tile i can be placed 0, 1 or 2 times from the player's rack onto the table
        List<Tile> tilesOnTable = currentState.fetchTilesOnTable();
        List<Tile> tilesOnRack = new ArrayList<>(List.copyOf(currentState.getRACKS().get(this)));
        for (Integer i : tileTypes.keySet()) {
            int timesOnTable = 0;
            int timesOnRack = 0;

            for (Tile tile : tileTypes.get(i)) {
                if (tilesOnTable.contains(tile)) {
                    timesOnTable++;
                    tilesOnTable.remove(tile);
                }
                if (tilesOnRack.contains(tile)) {
                    timesOnRack++;
                    tilesOnRack.remove(tile);
                }
            }

            MPVariable t_i = solver.makeIntVar(timesOnTable, timesOnTable, "t_" + i);
            MPVariable r_i = solver.makeIntVar(timesOnRack, timesOnRack, "r_" + i);
            MPVariable y_i = solver.makeIntVar(0, 2, "y_" + i);

            tValues.add(t_i);
            rValues.add(r_i);
            yValues.add(y_i);
        }

        // Objective function: maximize the number of tiles to draw from the player's rack onto the table
        MPObjective objective = solver.objective();
        for (MPVariable y_i : yValues) {
            objective.setCoefficient(y_i, 1);
        }
        objective.setMaximization();

        return null;
    }

    public boolean checkWin(GameState currentState) {
        return currentState.getRACKS().get(this).size() == 0;
    }

}
