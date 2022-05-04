package Players;

import Main.GameState;

import java.util.List;

public class RandomPlayer implements Player {

    private final int ID;
    private final String objectiveFunction;
    private boolean stuck;

    public RandomPlayer(int id, String objectiveFunction) {
        this.ID = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;
    }

    public String getName() {
        return "Random";
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
            // Return a randomly chosen move
            // Winning states are given priority
            for (GameState move : moves) {
                if (checkWin(move)) {
                    return move;
                }
            }

            int randomIndex = (int) (Math.random() * moves.size());
            return moves.get(randomIndex);
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
