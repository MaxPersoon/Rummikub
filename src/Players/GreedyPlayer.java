package Players;

import Main.GameState;

import java.util.List;

public class GreedyPlayer implements Player {

    private final int ID;
    private boolean stuck;

    public GreedyPlayer(int id) {
        this.ID = id;
        this.stuck = false;
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
        List<GameState> moves = currentState.getMoves(this);

        if (moves.size() >= 1) {
            // Returns the state with the smallest rack size for this player
            // Winning states are given priority
            GameState smallestRackState = null;
            int smallestRackSize = Integer.MAX_VALUE;

            for (GameState move : moves) {
                int playerRackSize = move.getRACKS().get(this).size();
                if (playerRackSize == 0) {
                    return move;
                }
                else if (playerRackSize < smallestRackSize) {
                    smallestRackState = move;
                    smallestRackSize = playerRackSize;
                }
            }

            smallestRackState.setParent(currentState); // // IMPORTANT FOR PROPER TERMINAL OUTPUT
            smallestRackState.printMoveInfo(this);
            return smallestRackState;
        }
        else {
            System.out.println("Player #" + ID + " is unable to make a move\n");
            this.stuck = true;
            return currentState;
        }
    }

    public boolean checkWin(GameState currentState) {
        return currentState.getRACKS().get(this).size() == 0;
    }

}
