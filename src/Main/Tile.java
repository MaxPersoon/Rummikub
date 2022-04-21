package Main;

import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final int ID;
    private final int NUMBER;
    private final String COLOUR;
    private final List<Set> SETS;
    private final ImageView IMAGE;

    public Tile(int id, int number, String colour, ImageView image) {
        this.ID = id;
        this.NUMBER = number;
        this.COLOUR = colour;
        this.SETS = new ArrayList<>();
        this.IMAGE = image;
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

    public ImageView getImage() {
        return IMAGE;
    }

    public void addSet(Set set) {
        this.SETS.add(set);
    }

    public void setImageCoordinates(double[] coordinates) {
        this.IMAGE.setX(coordinates[0]);
        this.IMAGE.setY(coordinates[1]);
        this.IMAGE.setVisible(true);
    }

    public void print() {
        System.out.println("Tile #" + this.ID + ": " + this.NUMBER + ", " + this.COLOUR);
    }

    public boolean isMatch(Tile tileToCheck) {
        return this.NUMBER == tileToCheck.NUMBER && this.COLOUR.equals(tileToCheck.COLOUR);
    }

}
