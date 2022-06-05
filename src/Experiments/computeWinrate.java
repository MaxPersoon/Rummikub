package Experiments;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class computeWinrate {

    private static final LinkedHashMap<String, double[]> playerData = new LinkedHashMap<>();

    public static void main(String[] args) {
        int[] validFiles = {1, 3, 5};

        for (int validFile : validFiles) {
            String fileName = "src/Experiments/data/" + validFile + "-moves.csv";
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
                fileName = "src/Experiments/data/" + validFile + "-winners.csv";
                lines = Files.readAllLines(Path.of(fileName));

                for (int j = 1; j < lines.size(); j++) {
                    String[] lineSplit = lines.get(j).split(",");
                    int gameId = Integer.parseInt(lineSplit[0]);
                    String winner = lineSplit[1];

                    String[] players = playersPerGame.get(gameId);
                    for (String player : players) {
                        fetchPlayerData(player)[1]++; // Up total count by one
                    }

                    fetchPlayerData(winner)[0]++; // Up win count by one
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Print data
        for (String player : playerData.keySet()) {
            double[] data = fetchPlayerData(player);
            double winPercentage = data[0] / data[1] * 100.0;

            System.out.print(player + ": ");
            System.out.format("%.2f", winPercentage);
            System.out.println("%");
        }
    }

    private static double[] fetchPlayerData(String player) {
        for (String key : playerData.keySet()) {
            if (key.equals(player)) {
                return playerData.get(player);
            }
        }

        playerData.put(player, new double[2]);
        return playerData.get(player);
    }

}
