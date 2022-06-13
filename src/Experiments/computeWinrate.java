package Experiments;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class computeWinrate {

    private static final LinkedHashMap<String, double[]> playerData = new LinkedHashMap<>();
    private static final LinkedHashMap<String, double[]> techniqueData = new LinkedHashMap<>();

    public static void main(String[] args) {
        String[] optimisationTechniques = {"AlphaBeta", "Greedy", "ILP"};
        String[] objectiveFunctions = {"ttc", "ttcwscm", "ttv", "ttvwscm"};

        for (String optimisationTechnique : optimisationTechniques) {
            techniqueData.put(optimisationTechnique, new double[2]);
            for (String objectiveFunction : objectiveFunctions) {
                String player = optimisationTechnique + "-" + objectiveFunction;
                playerData.put(player, new double[2]);
            }
        }

        int[] validFiles = {3, 5, 6, 7, 8, 9, 10};

        for (int validFile : validFiles) {
            String fileName = "src/Experiments/rawData/" + validFile + "-moves.csv";
            HashMap<Integer, String[]> playersPerGame = new HashMap<>();

            try {
                List<String> lines = Files.readAllLines(Path.of(fileName));

                // Fetch players per game
                int mostRecentGameId = 0;
                for (int j = 1; j < lines.size(); j++) {
                    String[] lineSplit = lines.get(j).split(",");
                    int gameId = Integer.parseInt(lineSplit[0]);

                    if (gameId > mostRecentGameId) {
                        String[] nextLineSplit = lines.get(j + 1).split(",");

                        String[] players = new String[2];
                        players[0] = lineSplit[1] + "-" + lineSplit[2];
                        players[1] = nextLineSplit[1] + "-" + nextLineSplit[2];

                        playersPerGame.put(gameId, players);

                        mostRecentGameId = gameId;
                    }
                }

                // Extract data
                fileName = "src/Experiments/rawData/" + validFile + "-winners.csv";
                lines = Files.readAllLines(Path.of(fileName));

                for (int j = 1; j < lines.size(); j++) {
                    String[] lineSplit = lines.get(j).split(",");
                    int gameId = Integer.parseInt(lineSplit[0]);
                    String winner = lineSplit[1];

                    // Determine loser
                    String[] players = playersPerGame.get(gameId);
                    String loser;
                    if (winner.equals(players[0])) {
                        loser = players[1];
                    } else {
                        loser = players[0];
                    }

                    // Update player data
                    updateData(playerData, winner, loser);

                    // Update technique data
                    String winnerTechnique = winner.split("-")[0];
                    String loserTechnique = loser.split("-")[0];
                    if (!winnerTechnique.equals(loserTechnique)) {
                        updateData(techniqueData, winnerTechnique, loserTechnique);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Write data
        File[] files = new File[2];
        files[0] = new File("src/Experiments/processedData/techniqueWinrate.csv");
        files[1] = new File("src/Experiments/processedData/playerWinrate.csv");

        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        writeData(techniqueData, files[0], false);
        writeData(playerData, files[1], true);
    }

    private static double[] fetchData(LinkedHashMap<String, double[]> data, String entry) {
        for (String key : data.keySet()) {
            if (key.equals(entry)) {
                return data.get(entry);
            }
        }

        return null; // Should never be reached
    }

    private static void updateData(LinkedHashMap<String, double[]> data, String winner, String loser) {
        double[] winnerData = fetchData(data, winner);
        double[] loserData = fetchData(data, loser);

        winnerData[0]++; // Add win
        winnerData[1]++; // Add game
        loserData[1]++; // Add game
    }

    private static void writeData(LinkedHashMap<String, double[]> data, File file, boolean includeObjectiveFunction) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.print("optimisationTechnique,");
            if (includeObjectiveFunction) {
                writer.print("objectiveFunction,");
            }
            writer.println("winrate (%)");

            for (String key : data.keySet()) {
                double[] keyData = fetchData(data, key);
                double winPercentage = keyData[0] / keyData[1] * 100.0;

                String[] keySplit = key.split("-");
                writer.print(keySplit[0] + ",");
                if (includeObjectiveFunction) {
                    writer.print(keySplit[1] + ",");
                }
                writer.format("%.2f%n", winPercentage);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
