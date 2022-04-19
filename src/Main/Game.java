package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

    private static final int PLAYERCOUNT = 2;

    public static void main(String[] args) {
        gameLoop();
    }

    public static void gameLoop() {
        // Create tiles
        List<Tile> tiles = new ArrayList<>();
        HashMap<String, List<Tile>> tilesWithColour = new HashMap<>();
        HashMap<Integer, List<Tile>> tilesWithNumber = new HashMap<>();

        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            tilesWithColour.put(colour, new ArrayList<>());
            for (int number = 1; number <= 13; number++) {
                Tile tile = new Tile(tiles.size() + 1, number, colour, PLAYERCOUNT);
                tiles.add(tile);
                tilesWithColour.get(colour).add(tile);
                if (!tilesWithNumber.containsKey(number)) {
                    tilesWithNumber.put(number, new ArrayList<>());
                }
                tilesWithNumber.get(number).add(tile);
            }
        }

        // Create groups
        List<Set> sets = new ArrayList<>();
        int idCounter = 0;
        for (int i = 1; i <= 13; i++) {
            List<Tile> tilesWithSpecificNumber = tilesWithNumber.get(i);
            List<Integer> idsWithSpecificNumber = new ArrayList<>();
            for (Tile tile : tilesWithSpecificNumber) {
                idsWithSpecificNumber.add(tile.getNUMBER());
            }

            idCounter++;
            Set set = new Set(idCounter, idsWithSpecificNumber);
            sets.add(set);

            for (Integer id : idsWithSpecificNumber) {
                List<Integer> idsWithSpecificNumberCopy = new ArrayList<>(List.copyOf(idsWithSpecificNumber));
                idsWithSpecificNumberCopy.remove(id);

                idCounter++;
                set = new Set(idCounter, idsWithSpecificNumberCopy);
                sets.add(set);
            }
        }

        // Create runs
        for (String colour : colours) {
            List<Tile> tilesWithSpecificColour = tilesWithColour.get(colour);
            List<Integer> idsWithSpecificColour = new ArrayList<>();
            for (Tile tile : tilesWithSpecificColour) {
                idsWithSpecificColour.add(tile.getNUMBER());
            }

            while (idsWithSpecificColour.size() >= 3) {
                List<Integer> run = new ArrayList<>();
                run.add(idsWithSpecificColour.remove(0));
                for (Integer id : idsWithSpecificColour) {
                    run.add(id);
                    if (run.size() >= 3) {
                        idCounter++;
                        Set set = new Set(idCounter, run);
                        sets.add(set);
                    }
                }
            }
        }
    }

}
