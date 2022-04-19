package Main;

import java.util.List;

public class Set {

    private final int ID;
    private final List<Integer> TILES; // IDs of tiles which occur in this set

    private int tableCounter;

    public Set(int id, List<Integer> tiles) {
        this.ID = id;
        this.TILES = tiles;
        this.tableCounter = 0;
    }

    public int getID() {
        return ID;
    }

    public List<Integer> getTILES() {
        return TILES;
    }

    public int getTableCounter() {
        return tableCounter;
    }

    public void setTableCounter(int tableCounter) {
        this.tableCounter = tableCounter;
    }

}
