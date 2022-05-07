package Players;

import Main.GameState;

public interface Player {

    String getName();

    int getID();

    String getObjectiveFunction();

    boolean isStuck();

    void stuck();

    void unstuck();

    GameState makeMove(GameState currentState);

    boolean checkWin(GameState state);

}
