package Main;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Application {

    public static final HashMap<Integer, List<double[]>> COORDINATES_RACKS = new HashMap<>(); // maps playerIDs to list of coordinates
    public static final List<List<double[]>> COORDINATES_TABLE = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        ObservableList<Node> nodes = root.getChildren();

        final double TILE_WIDTH = 25;
        final double TILE_HEIGHT = 50;
        final double TILE_OFFSET = 10;
        final double[] TABLE_START = {20, 170};
        final double TABLE_WIDTH = 1415;
        final double TABLE_HEIGHT = 730;
        final double[] RACK1_START = {20, 20};
        final double[] RACK2_START = {470, 20};
        final double RACK_WIDTH = 430;
        final double RACK_HEIGHT = 130;
        final double[] POOL_START = {920, 20};
        final double POOL_WIDTH = 90;
        final double POOL_HEIGHT = 70;
        final Image DEFAULT = new Image(new FileInputStream("resources/tiles/default.png"));

        // Pool
        Rectangle pool = new Rectangle(POOL_START[0], POOL_START[1], POOL_WIDTH, POOL_HEIGHT);
        pool.setFill(Color.DARKBLUE);
        nodes.add(pool);
        ImageView defaultTile = new ImageView(DEFAULT);
        defaultTile.setX(POOL_START[0] + TILE_OFFSET);
        defaultTile.setY(POOL_START[1] + TILE_OFFSET);
        defaultTile.setFitWidth(TILE_WIDTH);
        defaultTile.setFitHeight(TILE_HEIGHT);
        defaultTile.setPreserveRatio(true);
        nodes.add(defaultTile);
        Text counter = new Text(POOL_START[0] + 2 * TILE_OFFSET + TILE_WIDTH, POOL_START[1] + TILE_OFFSET + 36, "-1");
        counter.setFont(Font.font("arial", FontWeight.NORMAL, FontPosture.REGULAR, 30));
        counter.setFill(Color.GOLD);
        nodes.add(counter);

        // Racks
        Rectangle rack1 = new Rectangle(RACK1_START[0], RACK1_START[1], RACK_WIDTH, RACK_HEIGHT);
        rack1.setFill(Color.BROWN);
        nodes.add(rack1);
        COORDINATES_RACKS.put(1, new ArrayList<>());
        Rectangle rack2 = new Rectangle(RACK2_START[0], RACK2_START[1], RACK_WIDTH, RACK_HEIGHT);
        rack2.setFill(Color.BROWN);
        nodes.add(rack2);
        COORDINATES_RACKS.put(2, new ArrayList<>());
        for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 12; columnIndex++) {
                // Tile in rack 1
                double startX = RACK1_START[0] + TILE_OFFSET + columnIndex * (TILE_OFFSET + TILE_WIDTH);
                double startY = RACK1_START[1] + TILE_OFFSET + rowIndex * (TILE_OFFSET + TILE_HEIGHT);
                double[] coordinate = {startX, startY};
                COORDINATES_RACKS.get(1).add(coordinate);

                // Tile in rack 2
                startX = RACK2_START[0] + TILE_OFFSET + columnIndex * (TILE_OFFSET + TILE_WIDTH);
                startY = RACK2_START[1] + TILE_OFFSET + rowIndex * (TILE_OFFSET + TILE_HEIGHT);
                coordinate = new double[]{startX, startY};
                COORDINATES_RACKS.get(2).add(coordinate);
            }
        }

        // Table
        Rectangle table = new Rectangle(TABLE_START[0], TABLE_START[1], TABLE_WIDTH, TABLE_HEIGHT);
        table.setFill(Color.PURPLE);
        nodes.add(table);
        List<double[]> coordinatesSet = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < 12; rowIndex++) {
            for(int columnIndex = 0; columnIndex < 39; columnIndex++) {
                if (!(rowIndex == 11 && columnIndex >= 26)) {
                    double startX = TABLE_START[0] + TILE_OFFSET + columnIndex * (TILE_OFFSET + TILE_WIDTH) + Math.floor(columnIndex / 13d) * 20;
                    double startY = TABLE_START[1] + TILE_OFFSET + rowIndex * (TILE_OFFSET + TILE_HEIGHT);
                    double[] coordinate = {startX, startY};
                    coordinatesSet.add(coordinate);

                    if (coordinatesSet.size() == 13) {
                        COORDINATES_TABLE.add(coordinatesSet);
                        coordinatesSet = new ArrayList<>();
                    }
                }
            }
        }

        // Tiles
        List<ImageView> tileImages = new ArrayList<>();
        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            for (int number = 1; number <= 13; number++) {
                for (int i = 0; i < 2; i++) {
                    ImageView tileImage = new ImageView(new Image(new FileInputStream("resources/tiles/" + colour + "-" + number + ".png")));
                    tileImage.setFitWidth(TILE_WIDTH);
                    tileImage.setFitHeight(TILE_HEIGHT);
                    tileImage.setPreserveRatio(true);
                    tileImage.setVisible(false);
                    tileImages.add(tileImage);
                }
            }
        }
        nodes.addAll(tileImages);

//        for (int playerID : COORDINATES_RACKS.keySet()) {
//            System.out.println("Coordinates rack #" + playerID + ":");
//            for (double[] coordinate : COORDINATES_RACKS.get(playerID)) {
//                System.out.println("{" + coordinate[0] + ", " + coordinate[1] + "}");
//            }
//            System.out.println();
//        }
//
//        System.out.println("Coordinates table:");
//        int setCounter = 0;
//        for (List<double[]> coordinatesSet_ : COORDINATES_TABLE) {
//            setCounter++;
//            System.out.println("Set #" + setCounter + ":");
//            for (double[] coordinate : coordinatesSet_) {
//                System.out.println("{" + coordinate[0] + ", " + coordinate[1] + "}");
//            }
//            System.out.println();
//        }

        Scene scene = new Scene(root, 1455, 920);
        stage.setTitle("Rummikub - Game");
        stage.setScene(scene);
        stage.show();

        Game.tileImages = tileImages;
        Game game = new Game();
        game.start();
    }

}
