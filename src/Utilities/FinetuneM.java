package Utilities;

import Main.Game;
import Players.IntegerLinearProgramming;

import java.util.LinkedHashMap;

public class FinetuneM {

    public static void main(String[] args) {
        Game.playerTypes = new String[]{"ilp", "ilp"};
        Game.objectiveFunctions = new String[]{"ttc", "ttcwogm"};
        Game.finetuning = true;

        int numberOfGames = 20;
        LinkedHashMap<Integer, Integer> winCounterPerM = new LinkedHashMap<>();

        for (int M = 36; M > 0; M-=5) {
            IntegerLinearProgramming.wogmM = M;
            int winCounter = 0;

            for (int i = 0; i < numberOfGames; i++) {
                Game game = new Game();
                game.start();

                while (true) {
                    if (!game.isAlive()) {
                        System.out.println();
                        break;
                    }
                }

                IntegerLinearProgramming.tileTypes.clear();
                IntegerLinearProgramming.sets.clear();

                if (Game.players.get(1).checkWin(Game.currentState)) {
                    winCounter++;
                }
            }

            winCounterPerM.put(M, winCounter);
        }

        System.out.println("\n--- RESULTS ---");
        for (Integer M : winCounterPerM.keySet()) {
            double winPercentage = (double) winCounterPerM.get(M) / (double) numberOfGames * 100;
            System.out.println("> " + M + ": " + String.format("%,.2f", winPercentage)  + "%");
        }
    }

}
