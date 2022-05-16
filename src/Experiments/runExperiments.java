package Experiments;

import Main.Game;
import Players.IntegerLinearProgramming;
import Players.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class runExperiments {

    private static final String fileName = "1";
    private static final int numberOfGamesPerCombo = 10;

    private static FileWriter fileWriterMove;
    private static FileWriter fileWriterWinners;
    private static int gameCounter;

    public static void main(String[] args) {
        String pathnameMove = "src/Experiments/data/" + fileName + "-moves.csv";
        String pathnameWinners = "src/Experiments/data/" + fileName + "-winners.csv";

        try {
            File fileMove = new File(pathnameMove);
            File fileWinners = new File(pathnameWinners);

            if (fileMove.createNewFile()) {
                fileWriterMove = new FileWriter(fileMove);
            } else {
                System.out.println("\"" + pathnameMove + "\" already exists");
                System.exit(0);
            }

            if (fileWinners.createNewFile()) {
                fileWriterWinners = new FileWriter(fileWinners);
            } else {
                System.out.println("\"" + pathnameWinners + "\" already exists");
                System.exit(0);
            }

            fileWriterMove.write("gameCounter,turnCounter,optimisationTechnique,objectiveFunction,score,computationTime\n");
            fileWriterWinners.write("gameCounter,winners\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        final String[] allPlayerTypes = {"ilp"};
        final String[] allObjectiveFunctions = {"ttc", "ttv", "ttcwscm", "ttvwscm"};

        String[] playerTypes = new String[2];
        Game.playerTypes = playerTypes;
        String[] objectiveFunctions = new String[2];
        Game.objectiveFunctions = objectiveFunctions;
        Game.experimenting = true;

        gameCounter = 1;
        for (int i = 0; i < allPlayerTypes.length; i++) {
            for (int j = i; j < allPlayerTypes.length; j++) {
                String playerType1 = allPlayerTypes[i];
                String playerType2 = allPlayerTypes[j];

                playerTypes[0] = playerType1;
                playerTypes[1] = playerType2;

                for (int k = 0; k < allObjectiveFunctions.length; k++) {
                    for (int l = k; l < allObjectiveFunctions.length; l++) {
                        String objectiveFunctionPlayer1 = allObjectiveFunctions[k];
                        String objectiveFunctionPlayer2 = allObjectiveFunctions[l];

                        if (playerType1.equals(playerType2)) {
                            if (objectiveFunctionPlayer1.equals(objectiveFunctionPlayer2)) {
                                continue;
                            }
                        }

                        objectiveFunctions[0] = objectiveFunctionPlayer1;
                        objectiveFunctions[1] = objectiveFunctionPlayer2;

                        for (int m = 0; m < numberOfGamesPerCombo; m++) {
                            System.out.println("+++ GAME #" + gameCounter + " +++");

                            Game game = new Game();
                            game.start();

                            while (true) {
                                if (!game.isAlive()) {
                                    System.out.println();
                                    break;
                                }
                            }

                            if (playerType1.equals("ilp") || playerType2.equals("ilp")) {
                                IntegerLinearProgramming.tileTypes.clear();
                                IntegerLinearProgramming.sets.clear();
                            }

                            gameCounter++;
                        }
                    }
                }
            }
        }

        try {
            fileWriterMove.close();
            fileWriterWinners.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMoveToFile(int turnCounter, String optimisationTechnique, String objectiveFunction, double score, long computationTime) {
        String entry = gameCounter + "," + turnCounter + "," + optimisationTechnique + "," + objectiveFunction + "," + score + "," + computationTime + "\n";
        try {
            fileWriterMove.write(entry);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void writeWinnersToFile(List<Player> winners) {
        StringBuilder entry = new StringBuilder();
        entry.append(gameCounter).append(",");
        for (int i = 0; i < winners.size(); i++) {
            Player winner = winners.get(i);
            String playerType = winner.getName();
            String objectiveFunction = winner.getObjectiveFunction();
            entry.append(playerType).append("-").append(objectiveFunction);
            if (i < winners.size() - 1) {
                entry.append(";");
            }
        }
        entry.append("\n");

        try {
            fileWriterWinners.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
