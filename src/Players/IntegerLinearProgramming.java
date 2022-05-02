package Players;

import Main.GameState;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class IntegerLinearProgramming implements Player {

    private final int ID;
    private boolean stuck;

    public IntegerLinearProgramming(int id) {
        this.ID = id;
        this.stuck = false;
        Loader.loadNativeLibraries();
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
        long startTime = System.currentTimeMillis();
        // Create the linear solver with the SCIP backend.
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return null;
        }

        double infinity = java.lang.Double.POSITIVE_INFINITY;
        // x and y are integer non-negative variables.
        MPVariable x = solver.makeIntVar(0.0, infinity, "x");
        MPVariable y = solver.makeIntVar(0.0, infinity, "y");

        System.out.println("Number of variables = " + solver.numVariables());

        // x + 7 * y <= 17.5.
        MPConstraint c0 = solver.makeConstraint(-infinity, 17.5, "c0");
        c0.setCoefficient(x, 1);
        c0.setCoefficient(y, 7);

        // x <= 3.5.
        MPConstraint c1 = solver.makeConstraint(-infinity, 3.5, "c1");
        c1.setCoefficient(x, 1);
        c1.setCoefficient(y, 0);

        System.out.println("Number of constraints = " + solver.numConstraints());

        // Maximize x + 10 * y.
        MPObjective objective = solver.objective();
        objective.setCoefficient(x, 1);
        objective.setCoefficient(y, 10);
        objective.setMaximization();

        final MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Solution:");
            System.out.println("Objective value = " + objective.value());
            System.out.println("x = " + x.solutionValue());
            System.out.println("y = " + y.solutionValue());
        } else {
            System.err.println("The problem does not have an optimal solution!");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime));

        return null;
    }

    public boolean checkWin(GameState currentState) {
        return currentState.getRACKS().get(this).size() == 0;
    }

    public static void main(String[] args) {
        IntegerLinearProgramming ilp = new IntegerLinearProgramming(1);
        ilp.makeMove(null);
    }

}
