package Main;

import java.util.ArrayList;
import java.util.List;

public class Set {

    private final int ID;
    private final String TYPE; // "group" or "run"
    private final List<List<Tile>> TILES;

    public Set(int id, String type, List<Tile> tiles) {
        this.ID = id;
        this.TYPE = type;
        TILES = new ArrayList<>();
        for (Tile tile : tiles) {
            List<Tile> copies = new ArrayList<>();
            copies.add(tile);
            copies.add(Game.TILES.get(tile.getID()));
            TILES.add(copies);
            for (Tile copy : copies) {
                copy.addSet(this);
            }
        }
    }

    public int getID() {
        return ID;
    }

    public String getTYPE() {
        return TYPE;
    }

    public List<List<Tile>> getTILES() {
        return TILES;
    }

    public List<Tile> isExactMatch(List<Tile> tilesToCheck) {
        List<Tile> tilesInSet = new ArrayList<>();

        if (tilesToCheck.size() == TILES.size()) {
            tilesInSet = checkMatchingTiles(tilesToCheck);
        }

        return tilesInSet;
    }

    public List<Tile> drawableFromRack(List<Tile> rack) {
        // If drawable, returns all tiles which must be removed from the rack
        // If not drawable, returns an empty list
        List<Tile> tilesInSet = new ArrayList<>();

        if (rack.size() >= TILES.size()) {
            tilesInSet = checkMatchingTiles(rack);
        }

        return tilesInSet;
    }

    public List<Tile> checkMatchingTiles(List<Tile> tilesToCheck) {
        // Returns a list of tiles from tilesToCheck needed to construct this set
        // If this set cannot be constructed using the tiles in tilesToCheck, tilesInSet will be empty
        List<Tile> tilesInSet = new ArrayList<>();

        List<Tile> remainingTiles = new ArrayList<>(List.copyOf(tilesToCheck));
        for (List<Tile> copies : TILES) {
            boolean copyFound = false;

            for (Tile copy : copies) {
                if (remainingTiles.contains(copy)) {
                    copyFound = true;
                    tilesInSet.add(copy);
                    remainingTiles.remove(copy);
                    break;
                }
            }

            if (!copyFound) {
                tilesInSet.clear();
                break;
            }
        }

        return tilesInSet;
    }

    public boolean isExpandingTile(Tile tileToCheck) {
        if (TYPE.equals("group")) {
            if (TILES.size() == 3) {
                if (tileToCheck.getCOLOUR().equals("joker")) {
                    return true;
                }

                int tileNumber = 0;
                for (List<Tile> copies : TILES) {
                    Tile copy = copies.get(0);
                    String copyColour = copy.getCOLOUR();

                    if (!copyColour.equals("joker")) {
                        tileNumber = copy.getNUMBER();
                        if (tileToCheck.getCOLOUR().equals(copyColour)) {
                            return false;
                        }
                    }
                }

                return tileToCheck.getNUMBER() == tileNumber;
            }
        } else {
            // run
            if (TILES.size() < 13) {
                if (tileToCheck.getCOLOUR().equals("joker")) {
                    return true;
                }

                int firstTileNumber = 0;
                int lastTileNumber = 0;
                for (int i = 0; i < TILES.size(); i++) {
                    Tile copy = TILES.get(i).get(0);
                    String copyColour = copy.getCOLOUR();

                    if (!copyColour.equals("joker")) {
                        if (!tileToCheck.getCOLOUR().equals(copyColour)) {
                            return false;
                        }

                        int copyNumber = copy.getNUMBER();
                        firstTileNumber = copyNumber - i;
                        lastTileNumber = copyNumber + (TILES.size() - 1 - i);

                        break;
                    }
                }

                return tileToCheck.getNUMBER() == firstTileNumber - 1 || tileToCheck.getNUMBER() == lastTileNumber + 1;
            }
        }

        return false;
    }

    public void print() {
        StringBuilder text = new StringBuilder();
        for (List<Tile> allCopies : TILES) {
            Tile copy1 = allCopies.get(0);
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(copy1.getNUMBER() + ", " + copy1.getCOLOUR());
        }
        System.out.println("Set #" + this.ID + ": " + text);
    }

    public void printAllCopies() {
        StringBuilder text = new StringBuilder();
        for (List<Tile> allCopies : TILES) {
            StringBuilder subtext = new StringBuilder();
            for (Tile copy : allCopies) {
                if (subtext.isEmpty()) {
                    subtext.append("(1st) ");
                }
                else {
                    subtext.append(" (2nd) ");
                }
                subtext.append(copy.getNUMBER() + ", " + copy.getCOLOUR());
            }
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(subtext);
        }
        System.out.println("Set #" + this.ID + ": " + text);
    }

}
