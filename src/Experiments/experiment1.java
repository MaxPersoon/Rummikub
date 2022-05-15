package Experiments;

import Main.Game;

public class experiment1 {

    private static final int numberOfGamesPerCombo = 10;

    public static void main(String[] args) {
        final String[] allObjectiveFunctions = {"ttc", "ttv", "ttcwscm", "ttvwscm"};

        int gameCounter = 1;
        for (String objectiveFunctionPlayer1 : allObjectiveFunctions) {
            for (String objectiveFunctionPlayer2 : allObjectiveFunctions) {
                if (!objectiveFunctionPlayer1.equals(objectiveFunctionPlayer2)) {
                    String[] playerTypes = new String[2];
                    playerTypes[0] = "ilp";
                    playerTypes[1] = "ilp";

                    String[] objectiveFunctions = new String[2];
                    objectiveFunctions[0] = objectiveFunctionPlayer1;
                    objectiveFunctions[1] = objectiveFunctionPlayer2;

                    Game.playerTypes = playerTypes;
                    Game.objectiveFunctions = objectiveFunctions;
                    Game.experimenting = true;

                    for (int i = 0; i < numberOfGamesPerCombo; i++) {
                        System.out.println("+++ GAME #" + gameCounter + " +++");

                        Game game = new Game();
                        game.start();

                        while (true) {
                            if (!game.isAlive()) {
                                System.out.println();
                                break;
                            }
                        }

                        gameCounter++;
                    }
                }
            }
        }
    }

}
