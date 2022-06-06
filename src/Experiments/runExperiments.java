package Experiments;

import Main.Game;
import Players.IntegerLinearProgramming;
import Players.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class runExperiments {

    private static final String fileName = "6";
    private static final int numberOfGamesPerCombo = 5;

    private static FileWriter fileWriterMove;
    private static FileWriter fileWriterWinners;
    private static int gameCounter;

    public static void main(String[] args) {
        String pathnameMove = "src/Experiments/rawData/" + fileName + "-moves.csv";
        String pathnameWinners = "src/Experiments/rawData/" + fileName + "-winners.csv";

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

            fileWriterMove.write("gameID,optimisationTechnique,objectiveFunction,score,computationTime\n");
            fileWriterWinners.write("gameID,winners\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        final String[] allPlayerTypes = {"greedy", "alphabeta", "ilp"};
        final String[] allObjectiveFunctions = {"ttc", "ttv", "ttcwscm", "ttvwscm"};

        String[] playerTypes = new String[2];
        Game.playerTypes = playerTypes;
        String[] objectiveFunctions = new String[2];
        Game.objectiveFunctions = objectiveFunctions;
        Game.experimenting = true;

        gameCounter = 1;
        long startTime = System.currentTimeMillis();
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

                            long elapsedTime = System.currentTimeMillis() - startTime;
                            printTimeRemaining(gameCounter, (double) elapsedTime);

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

    public static void writeMoveToFile(String optimisationTechnique, String objectiveFunction, double score, long computationTime) {
        String entry = gameCounter + "," + optimisationTechnique + "," + objectiveFunction + "," + score + "," + computationTime + "\n";
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

    private static void printTimeRemaining(int gameCounter, double elapsedTime) {
        int totalNumberOfGames = 48 * numberOfGamesPerCombo;

        elapsedTime = elapsedTime / 1000.0 / 60.0; // convert ms to minutes
        double averageTimePerGame = elapsedTime / gameCounter;
        double timeRemaining = averageTimePerGame * (totalNumberOfGames - gameCounter);

        int hours = (int) Math.floor(timeRemaining / 60.0);
        int minutes = (int) (timeRemaining - hours * 60);

        System.out.println("Time left: " + hours + " hours and " + minutes + " minutes");
    }

}
