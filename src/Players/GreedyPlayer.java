package Players;

import Main.Game;
import Main.GameState;

import java.util.List;

public class GreedyPlayer implements Player {

    private final int id;
    private final String objectiveFunction;
    private boolean stuck;

    public GreedyPlayer(int id, String objectiveFunction) {
        this.id = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;
    }

    public String getName() {
        return "Greedy";
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
        List<GameState> moves = currentState.getMoves(this, this, objectiveFunction, 50, Game.maximumMoveTime);

        if (moves.size() >= 1) {
            // Returns the state with the highest score
            GameState highestScoreState = null;
            double highestScore = Double.NEGATIVE_INFINITY;

            for (GameState move : moves) {
                double score = move.getScore();

                if (score > highestScore) {
                    highestScoreState = move;
                    highestScore = score;
                }
            }

            return highestScoreState;
        }
        else {
            return currentState;
        }
    }

    public boolean checkWin(GameState state) {
        return state.getRacks().get(this).size() == 0;
    }

}
