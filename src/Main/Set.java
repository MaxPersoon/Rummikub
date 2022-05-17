package Main;

import java.util.ArrayList;
import java.util.List;

public class Set {

    private final int id;
    private final String type; // "group" or "run"
    private final List<List<Tile>> tiles;
    private final String signature;

    public Set(int id, String type, List<Tile> tilesInSet, String signature) {
        this.id = id;
        this.type = type;
        this.tiles = new ArrayList<>();
        for (Tile tile : tilesInSet) {
            List<Tile> copies = new ArrayList<>();
            copies.add(tile);
            copies.add(Game.tiles.get(tile.getId()));
            this.tiles.add(copies);
            for (Tile copy : copies) {
                copy.addSet(this);
            }
        }
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<List<Tile>> getTiles() {
        return tiles;
    }

    public String getSignature() {
        return signature;
    }

    public List<Tile> isExactMatch(List<Tile> tilesToCheck) {
        List<Tile> tilesInSet = new ArrayList<>();

        if (tilesToCheck.size() == tiles.size()) {
            tilesInSet = checkMatchingTiles(tilesToCheck);
        }

        return tilesInSet;
    }

    public List<Tile> drawableFromRack(List<Tile> rack) {
        // If drawable, returns all tiles which must be removed from the rack
        // If not drawable, returns an empty list
        List<Tile> tilesInSet = new ArrayList<>();

        if (rack.size() >= tiles.size()) {
            tilesInSet = checkMatchingTiles(rack);
        }

        return tilesInSet;
    }

    public List<Tile> checkMatchingTiles(List<Tile> tilesToCheck) {
        // Returns a list of tiles from tilesToCheck needed to construct this set
        // If this set cannot be constructed using the tiles in tilesToCheck, tilesInSet will be empty
        List<Tile> tilesInSet = new ArrayList<>();

        List<Tile> remainingTiles = new ArrayList<>(List.copyOf(tilesToCheck));
        for (List<Tile> copies : tiles) {
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
        if (type.equals("group")) {
            if (tiles.size() == 3) {
                if (tileToCheck.getColour().equals("joker")) {
                    return true;
                }

                int tileNumber = 0;
                for (List<Tile> copies : tiles) {
                    Tile copy = copies.get(0);
                    String copyColour = copy.getColour();

                    if (!copyColour.equals("joker")) {
                        tileNumber = copy.getNumber();
                        if (tileToCheck.getColour().equals(copyColour)) {
                            return false;
                        }
                    }
                }

                return tileToCheck.getNumber() == tileNumber;
            }
        } else {
            // run
            if (tiles.size() < 13) {
                if (tileToCheck.getColour().equals("joker")) {
                    return true;
                }

                int firstTileNumber = 0;
                int lastTileNumber = 0;
                for (int i = 0; i < tiles.size(); i++) {
                    Tile copy = tiles.get(i).get(0);
                    String copyColour = copy.getColour();

                    if (!copyColour.equals("joker")) {
                        if (!tileToCheck.getColour().equals(copyColour)) {
                            return false;
                        }

                        int copyNumber = copy.getNumber();
                        firstTileNumber = copyNumber - i;
                        lastTileNumber = copyNumber + (tiles.size() - 1 - i);

                        break;
                    }
                }

                return tileToCheck.getNumber() == firstTileNumber - 1 || tileToCheck.getNumber() == lastTileNumber + 1;
            }
        }

        return false;
    }

    public void print() {
        StringBuilder text = new StringBuilder();
        for (List<Tile> copies : tiles) {
            Tile copy = copies.get(0);
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(copy.getNumber()).append(", ").append(copy.getColour());
        }
        System.out.println("Set #" + this.id + ": " + text);
    }

    public void printAllCopies() {
        StringBuilder text = new StringBuilder();
        for (List<Tile> copies : tiles) {
            StringBuilder subtext = new StringBuilder();
            for (Tile copy : copies) {
                if (subtext.isEmpty()) {
                    subtext.append("(1st) ");
                }
                else {
                    subtext.append(" (2nd) ");
                }
                subtext.append(copy.getNumber()).append(", ").append(copy.getColour());
            }
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(subtext);
        }
        System.out.println("Set #" + this.id + ": " + text);
    }

}
