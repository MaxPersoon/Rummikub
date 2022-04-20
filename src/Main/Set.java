package Main;

import java.util.List;

public class Set {

    private final int ID;
    private final String TYPE; // "group" or "run"
    private final List<Tile> TILES;

    private int tableCounter;

    public Set(int id, String type, List<Tile> tiles) {
        this.ID = id;
        this.TYPE = type;
        this.TILES = tiles;
        for (Tile tile : tiles) {
            tile.addSet(this);
        }
        this.tableCounter = 0;
    }

    public int getID() {
        return ID;
    }

    public String getTYPE() {
        return TYPE;
    }

    public List<Tile> getTILES() {
        return TILES;
    }

    public int getTableCounter() {
        return tableCounter;
    }

    public void setTableCounter(int tableCounter) {
        this.tableCounter = tableCounter;
    }

    public void print() {
        StringBuilder text = new StringBuilder();
        for (Tile tile : TILES) {
            if (!text.isEmpty()) {
                text.append(" | ");
            }
            text.append(tile.getNUMBER() + ", " + tile.getCOLOUR());
        }
        System.out.println("Set #" + this.ID + ": " + text);
    }

    public boolean checkMatchingTiles(List<Tile> tilesToCheck) {
        for (Tile tileToCheck : tilesToCheck) {
            boolean match = false;
            for (Tile tile : TILES) {
                if (tileToCheck.checkMatchingTile(tile)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

}
