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
import java.util.LinkedHashMap;
import java.util.List;

public class Main extends Application {

    private static final String[] playerTypes = {"ilp", "ilp"};
    // greedy
    // alphabeta
    // ilp
    private static final String[] objectiveFunctions = {"ttc", "ttv"};
    // ttc = total tile count
    // ttv = total tile value
    // ttcwscm = total tile count with set change minimization
    // ttvwscm = total tile value with set change minimization
    public static final LinkedHashMap<Integer, List<double[]>> coordinatesRacks = new LinkedHashMap<>(); // maps playerIDs to list of coordinates
    public static final List<List<double[]>> coordinatesTable = new ArrayList<>(); // each entry is a list of coordinates forming a set

    public static void main(String[] args) {
        // Validity check
        int numberOfPlayers = playerTypes.length;
        int numberOfObjectiveFunctions = objectiveFunctions.length;

        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            System.out.println("Error: incorrect number of players");
            System.exit(0);
        }

        if (numberOfPlayers != numberOfObjectiveFunctions) {
            System.out.println("Error: incorrect number of objective functions (" + numberOfObjectiveFunctions + " instead of " + numberOfPlayers + ")");
            System.exit(0);
        }

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Setup visualization
        Group root = new Group();
        ObservableList<Node> nodes = root.getChildren();
        List<Node> nodesToPassThrough = new ArrayList<>();

        final double TILE_WIDTH = 25;
        final double TILE_HEIGHT = 50;
        final double TILE_OFFSET = 10;
        final double[] TABLE_START = {470, 20};
        final double TABLE_WIDTH = 1415;
        final double TABLE_HEIGHT = 730;
        final List<double[]> RACK_STARTS = new ArrayList<>();
        RACK_STARTS.add(new double[]{20, 110}); // Rack 1
        RACK_STARTS.add(new double[]{20, 260}); // Rack 2
        RACK_STARTS.add(new double[]{20, 410}); // Rack 3
        RACK_STARTS.add(new double[]{20, 560}); // Rack 4
        final double RACK_WIDTH = 430;
        final double RACK_HEIGHT = 130;
        final double[] POOL_START = {190, 20};
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
        nodesToPassThrough.add(counter);
        // Added to 'nodes' later on

        // Racks
        // Create a rack for each player
        for (int i = 0; i < playerTypes.length; i++) {
            Rectangle rack = new Rectangle(RACK_STARTS.get(i)[0], RACK_STARTS.get(i)[1], RACK_WIDTH, RACK_HEIGHT);
            rack.setFill(Color.BROWN);
            nodes.add(rack);
            coordinatesRacks.put(i + 1, new ArrayList<>());
        }

        // Generate coordinates of tiles in each rack
        for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 12; columnIndex++) {
                for (int i = 0; i < playerTypes.length; i++) {
                    double startX = RACK_STARTS.get(i)[0] + TILE_OFFSET + columnIndex * (TILE_OFFSET + TILE_WIDTH);
                    double startY = RACK_STARTS.get(i)[1] + TILE_OFFSET + rowIndex * (TILE_OFFSET + TILE_HEIGHT);
                    double[] coordinate = {startX, startY};
                    coordinatesRacks.get(i + 1).add(coordinate);
                }
            }
        }

        // Debugging: print coordinates of tiles in each rack
//        for (int playerID : COORDINATES_RACKS.keySet()) {
//            System.out.println("Coordinates rack #" + playerID + ":");
//            for (double[] coordinate : COORDINATES_RACKS.get(playerID)) {
//                System.out.println("{" + coordinate[0] + ", " + coordinate[1] + "}");
//            }
//            System.out.println();
//        }

        // Table
        Rectangle table = new Rectangle(TABLE_START[0], TABLE_START[1], TABLE_WIDTH, TABLE_HEIGHT);
        table.setFill(Color.PURPLE);
        nodes.add(table);

        // Generate coordinates of sets on table
        List<double[]> coordinatesSet = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < 12; rowIndex++) {
            for(int columnIndex = 0; columnIndex < 39; columnIndex++) {
                if (!(rowIndex == 11 && columnIndex >= 26)) {
                    double startX = TABLE_START[0] + TILE_OFFSET + columnIndex * (TILE_OFFSET + TILE_WIDTH) + Math.floor(columnIndex / 13d) * 20;
                    double startY = TABLE_START[1] + TILE_OFFSET + rowIndex * (TILE_OFFSET + TILE_HEIGHT);
                    double[] coordinate = {startX, startY};
                    coordinatesSet.add(coordinate);

                    if (coordinatesSet.size() == 13) {
                        coordinatesTable.add(coordinatesSet);
                        coordinatesSet = new ArrayList<>();
                    }
                }
            }
        }

        // Debugging: print coordinates of sets on table
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

        // Generate tile images
        String[] colours = {"black", "red", "orange", "blue"};
        for (String colour : colours) {
            for (int number = 1; number <= 13; number++) {
                for (int i = 0; i < 2; i++) {
                    ImageView tileImage = new ImageView(new Image(new FileInputStream("resources/tiles/" + colour + "-" + number + ".png")));
                    tileImage.setFitWidth(TILE_WIDTH);
                    tileImage.setFitHeight(TILE_HEIGHT);
                    tileImage.setPreserveRatio(true);
                    tileImage.setVisible(false);
                    nodesToPassThrough.add(tileImage);
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            ImageView tileImage = new ImageView(new Image(new FileInputStream("resources/tiles/joker.png")));
            tileImage.setFitWidth(TILE_WIDTH);
            tileImage.setFitHeight(TILE_HEIGHT);
            tileImage.setPreserveRatio(true);
            tileImage.setVisible(false);
            nodesToPassThrough.add(tileImage);
        }

        nodes.addAll(nodesToPassThrough);

        Scene scene = new Scene(root, 1905, 770);
        stage.setTitle("Rummikub - Game");
        stage.setScene(scene);
        stage.show();

        // Launch the game
        Game.playerTypes = playerTypes;
        Game.objectiveFunctions = objectiveFunctions;
        Game.experimenting = false;
        Game.nodes = nodesToPassThrough;
        Game game = new Game();
        game.start();
    }

}
