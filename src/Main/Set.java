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

    public List<Tile> drawableFromRack(List<Tile> rack) {
        // If drawable, returns all tiles which must be removed from the rack
        // If not drawable, returns an empty list
        List<Tile> tilesInSet = new ArrayList<>();

        if (rack.size() >= TILES.size()) {
            tilesInSet = checkMatchingTiles(rack);
        }

        return tilesInSet;
    }

    public boolean isExpandingTile(Tile tileToCheck) {
        boolean isExpandingTile = false;

        if (TYPE.equals("group")) {
            if (TILES.size() == 3) {
                if (tileToCheck.getNUMBER() == TILES.get(0).get(0).getNUMBER()) {
                    isExpandingTile = true;
                    for (List<Tile> copies : TILES) {
                        if (tileToCheck.getCOLOUR().equals(copies.get(0).getCOLOUR())) {
                            isExpandingTile = false;
                            break;
                        }
                    }
                }
            }
        }
        else {
            // run
            if (TILES.size() < 13) {
                if (tileToCheck.getCOLOUR().equals(TILES.get(0).get(0).getCOLOUR())) {
                    if (tileToCheck.getNUMBER() == TILES.get(0).get(0).getNUMBER() - 1 || tileToCheck.getNUMBER() == TILES.get(TILES.size() - 1).get(0).getNUMBER() + 1) {
                        isExpandingTile = true;
                    }
                }
            }
        }

        return isExpandingTile;
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

    public List<Tile> checkMatchingTiles(List<Tile> tilesToCheck) {
        // Returns a list of tiles from tilesToCheck needed to construct this set
        // If this set cannot be constructed using the tiles in tilesToCheck, tilesInSet will be empty
        List<Tile> tilesInSet = new ArrayList<>();

        for (List<Tile> copies : TILES) {
            boolean copyFound = false;

            for (Tile copy : copies) {
                if (!copyFound) {
                    for (Tile tileToCheck : tilesToCheck) {
                        if (copy == tileToCheck) {
                            copyFound = true;
                            tilesInSet.add(copy);
                            break;
                        }
                    }
                }
            }

            if (!copyFound) {
                tilesInSet.clear();
                break;
            }
        }

        return tilesInSet;
    }

    public List<Tile> isExactMatch(List<Tile> tilesToCheck) {
        List<Tile> tilesInSet = new ArrayList<>();

        if (tilesToCheck.size() == TILES.size()) {
            tilesInSet = checkMatchingTiles(tilesToCheck);
        }

        return tilesInSet;
    }

}
