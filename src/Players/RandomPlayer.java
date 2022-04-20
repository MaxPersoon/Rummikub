package Players;

import Main.GameState;
import Main.Tile;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    public GameState makeMove(GameState currentState) {
        List<GameState> moves = currentState.getMoves(this);

        if (moves.size() >= 1) {
            // Filter out all game states in which an opponent won
            // If a game state in which this player wins is encountered, that game state is returned
            // Otherwise, a randomly chosen game state is chosen from the filtered set of game states
            List<GameState> temp = new ArrayList<>();
            for (GameState move : moves) {
                HashMap<Player, List<Tile>> racks = move.getRACKS();
                boolean keep = true;
                for (Player player : racks.keySet()) {
                    if (racks.get(player).size() == 0) {
                        if (player == this) {
                            return move;
                        } else {
                            keep = false;
                            break;
                        }
                    }
                }
                if (keep) {
                    temp.add(move);
                }
            }
            moves = temp;

            int randomIndex = (int) (Math.random() * moves.size());
            return moves.get(randomIndex);
        }
        else {
            System.out.println("Player #" + ID + " is unable to make a move");
            this.stuck = true;
            return currentState;
        }
    }

    @Override
    public boolean checkWin(GameState currentState) {
        return currentState.getRACKS().get(this).size() == 0;
    }

}
