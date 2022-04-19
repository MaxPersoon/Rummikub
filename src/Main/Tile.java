package Main;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final int ID;
    private final int NUMBER;
    private final String COLOUR;
    private final List<Integer> SETS; // IDs of sets in which this tile occurs

    private int tableCounter;
    private int[] rackCounters;

    public Tile(int id, int number, String colour, int playerCount) {
        this.ID = id;
        this.NUMBER = number;
        this.COLOUR = colour;
        this.SETS = new ArrayList<>();
        this.tableCounter = 0;
        this.rackCounters = new int[playerCount];
    }

    public int getID() {
        return ID;
    }

    public int getNUMBER() {
        return NUMBER;
    }

    public String getCOLOUR() {
        return COLOUR;
    }

    public List<Integer> getSETS() {
        return SETS;
    }

    public int getTableCounter() {
        return tableCounter;
    }

    public int[] getRackCounters() {
        return rackCounters;
    }

    public void addSet(int setID) {
        this.SETS.add(setID);
    }

    public void setTableCounter(int tableCounter) {
        this.tableCounter = tableCounter;
    }

    public void setRackCounter(int index, int rackCounter) {
        this.rackCounters[index] = rackCounter;
    }

}
