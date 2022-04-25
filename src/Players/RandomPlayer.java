package Players;

import Main.GameState;
import Main.Tile;

import java.util.List;

public class RandomPlayer implements Player {

    private final int ID;
    private boolean stuck;

    public RandomPlayer(int id) {
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
            // Return a randomly chosen move
            // Winning states are given priority
            for (GameState move : moves) {
                List<Tile> playerRack = move.getRACKS().get(this);
                if (playerRack.size() == 0) {
                    return move;
                }
            }

            int randomIndex = (int) (Math.random() * moves.size());
            GameState randomMove = moves.get(randomIndex);
            randomMove.setParent(currentState); // IMPORTANT FOR PROPER TERMINAL OUTPUT
            randomMove.printMoveInfo(this);
            return randomMove;
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
