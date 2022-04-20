package Main;

import Players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tile {

    private final int ID;
    private final int NUMBER;
    private final String COLOUR;
    private final List<Set> SETS;

    private boolean onTable;
    private final HashMap<Player, Integer> RACK_COUNTERS;

    public Tile(int id, int number, String colour, List<Player> players) {
        this.ID = id;
        this.NUMBER = number;
        this.COLOUR = colour;
        this.SETS = new ArrayList<>();
        this.onTable = false;
        this.RACK_COUNTERS = new HashMap<>();
        for (Player player : players) {
            RACK_COUNTERS.put(player, 0);
        }
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

    public List<Set> getSETS() {
        return SETS;
    }

    public boolean isOnTable() {
        return onTable;
    }

    public Integer getRackCounter(Player player) {
        return RACK_COUNTERS.get(player);
    }

    public void addSet(Set set) {
        this.SETS.add(set);
    }

    public void setOnTable(boolean onTable) {
        this.onTable = onTable;
    }

    public void increaseRackCounter(Player player, int increaseAmount) {
        int newAmount = this.RACK_COUNTERS.get(player) + increaseAmount;
        this.RACK_COUNTERS.replace(player, newAmount);
    }

    public void decreaseRackCounter(Player player, int decreaseAmount) {
        int newAmount = this.RACK_COUNTERS.get(player) - decreaseAmount;
        this.RACK_COUNTERS.replace(player, newAmount);
    }

    public void print() {
        System.out.println("Tile #" + this.ID + ": " + this.NUMBER + ", " + this.COLOUR);
    }

    public Tile makeCopy(int id) {
        return new Tile(id, this.NUMBER, this.COLOUR, Game.PLAYERS);
    }

    public boolean checkMatchingTile(Tile tileToCheck) {
        return this.NUMBER == tileToCheck.NUMBER && this.COLOUR.equals(tileToCheck.COLOUR);
    }

}
