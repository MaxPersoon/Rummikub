package Main;

import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final int id;
    private final int number;
    private final String colour;
    private final List<Set> sets;
    private ImageView image;

    public Tile(int id, int number, String colour) {
        this.id = id;
        this.number = number;
        this.colour = colour;
        this.sets = new ArrayList<>();
        this.image = null;
    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getColour() {
        return colour;
    }

    public List<Set> getSets() {
        return sets;
    }

    public ImageView getImage() {
        return image;
    }

    public void addSet(Set set) {
        this.sets.add(set);
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public void setImageCoordinates(double[] coordinates) {
        this.image.setX(coordinates[0]);
        this.image.setY(coordinates[1]);
        this.image.setVisible(true);
    }

    public void print() {
        System.out.println("Tile #" + this.id + ": " + this.number + ", " + this.colour);
    }

    public boolean isMatch(Tile tileToCheck) {
        return this.number == tileToCheck.number && this.colour.equals(tileToCheck.colour);
    }

}
