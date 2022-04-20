package Players;

import Main.GameState;

public interface Player {

    int getID();

    boolean isStuck();

    void unstuck();

    GameState makeMove(GameState currentState);

    boolean checkWin(GameState currentState);

}
