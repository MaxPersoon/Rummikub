package Players;

import Main.GameState;

import java.util.List;

public class GreedyPlayer implements Player {

    private final int ID;
    private final String objectiveFunction;
    private boolean stuck;

    public GreedyPlayer(int id, String objectiveFunction) {
        this.ID = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;
    }

    public String getName() {
        return "Greedy";
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
        List<GameState> moves = currentState.getMoves(this);

        if (moves.size() >= 1) {
            // Returns the state with the highest score
            // Winning states are given priority
            GameState highestScoreState = null;
            int highestScore = Integer.MIN_VALUE;

            for (GameState move : moves) {
                int score = move.getScore();

                if (checkWin(move)) {
                    return move;
                }
                else if (score > highestScore) {
                    highestScoreState = move;
                    highestScore = score;
                }
            }

            return highestScoreState;
        }
        else {
            System.out.println("Player #" + ID + " is unable to make a move\n");
            this.stuck = true;
            return currentState;
        }
    }

    public boolean checkWin(GameState state) {
        return state.getRACKS().get(this).size() == 0;
    }

}
