import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

/**
 * FloodMoveEmulator
 *
 * @author - Sean McGrath
 */
public class Controller {

    @FXML
    public AnchorPane Container;
    @FXML
    public Button forwardButton;
    @FXML
    public Button backButton;
    @FXML
    public TextField parallelism;
    @FXML
    public TextField size;
    @FXML
    public GridPane colorGrid;
    @FXML
    public TextField depth;
    @FXML
    public Label moveCountLabel;
    @FXML
    public Button startButton;
    @FXML
    public Button generateButton;

    public static int MAX_DEPTH;

    @FXML
    protected void initialize(){
        parallelism.setTextFormatter(textFormatter1);
        size.setTextFormatter(textFormatter2);
        depth.setTextFormatter(textFormatter3);
    }

    UnaryOperator<TextFormatter.Change> filter = change -> {
        String text = change.getText();

        if (text.matches("[0-8]*")) {
            return change;
        }

        return null;
    };
    TextFormatter<String> textFormatter1 = new TextFormatter<>(filter);
    TextFormatter<String> textFormatter2 = new TextFormatter<>(filter);
    TextFormatter<String> textFormatter3 = new TextFormatter<>(filter);

    //number of processors to be used by JoinForkpool
    private int p;
    //size of board is n*n
    private int n;

    //used for displaying
    ArrayList<Puzzle> currentResults;
    //index of current display
    int index;

    private static Color[][] origin;

    /**
     * generates initial
     */
    public void generatePressed(){
        //disable next+prev buttons if they were not already disabled
        backButton.setDisable(true);
        forwardButton.setDisable(true);
        moveCountLabel.setText("Moves: ");//reset move label to default

        //get size and parallelism from gui, default 7x7 and 2 parallelism
        n = Integer.parseInt(size.getText());

        //set grid size
        colorGrid.getChildren().clear();
        Color[][] tmp = generateBoard();
        renderGrid(tmp);
        origin = tmp;
        //un-disable start
        startButton.setDisable(false);

    }

    public void startPressed(){
        //disable start so multiple runs cant be done at the same time
        startButton.setDisable(true);
        //disable generate button until run is done
        generateButton.setDisable(true);

        MAX_DEPTH = Integer.parseInt(depth.getText());
        this.p = Integer.parseInt(parallelism.getText());

        //run algorithm
        Puzzle p = new Puzzle(origin);
        Runner r = new Runner(p,  1, MAX_DEPTH);

        //pool has parallelism P
        ForkJoinPool pool = new ForkJoinPool(this.p);

        Puzzle result = pool.invoke(r);

        while (!result.isDone()){//while result is not final, keep pooling
            Runner tmpR = new Runner(result,  1, MAX_DEPTH);
            pool = new ForkJoinPool(this.p);
            result = pool.invoke(tmpR);
        }

        ArrayList<Puzzle> results = new ArrayList<>();
        for (; result != null; result = result.memory) {//grab list
            results.add(result);
        }

        moveCountLabel.setText("Moves: " + Integer.toString(results.size()));
        currentResults = results;
        index = currentResults.size()-1;

        //allow new generations
        generateButton.setDisable(false);
        backButton.setDisable(false);
        forwardButton.setDisable(false);
    }

    public void prevPressed(){
        if(index+1 < currentResults.size()) {
            index++;
            renderGrid(currentResults.get(index).board);
        }
    }

    public void nextPressed(){
        if(index-1 >= 0) {
            index--;
            renderGrid(currentResults.get(index).board);
        }
    }

    /**
     * renders board on GUI
     * assumes variable n is already set
     * @param board - colors to be set on gridpane
     */
    private void renderGrid(Color[][] board){
        colorGrid.getChildren().clear();

        //remove old grid
        while(colorGrid.getRowConstraints().size() > 0){
            colorGrid.getRowConstraints().remove(0);
        }
        while(colorGrid.getColumnConstraints().size() > 0){
            colorGrid.getColumnConstraints().remove(0);
        }

        colorGrid.setHgap(.8); //horizontal gap in pixels => that's what you are asking for
        colorGrid.setVgap(.8); //vertical gap in pixels
        colorGrid.setPadding(new Insets(5, 5, 5, 5));

        for (int i = 0; i < n; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / n);
            colorGrid.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < n; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / n);
            colorGrid.getRowConstraints().add(rowConst);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Pane tmp = new Pane();
                tmp.setPrefHeight(580.0/n);
                tmp.setPrefWidth(660.0/n);


                tmp.setStyle(setColor(board[i][j]));
                colorGrid.add(tmp, i, j);
            }
        }
    }

    /**
     * generates a randomized board of size n by n
     * @return - finished puzzle board
     */
    private Color[][] generateBoard(){
        Color[][] result = new Color[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = getRandomColor();
            }
        }
        return result;
    }

    /**
     * Changes color to string for javafx css, so that GUI can be populated
     * @param c - color enum to be used
     * @return - string for styles
     */
    private String setColor(Color c){
        switch (c){
            case BLUE: return "-fx-background-color: blue;";
            case GREEN: return "-fx-background-color: green;";
            case ORANGE: return "-fx-background-color: orange;";
            case PURPLE: return "-fx-background-color: purple;";
            case RED: return "-fx-background-color: red;";
            case YELLOW: return "-fx-background-color: yellow;";
            default: return null;
        }
    }

    /**
     * gets a random color enum
     * @return - random color
     */
    private Color getRandomColor(){
        int rand = ThreadLocalRandom.current().nextInt(1, 7);
        switch (rand){
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.ORANGE;
            case 4: return Color.PURPLE;
            case 5: return Color.RED;
            case 6: return Color.YELLOW;
            default: return null;
        }
    }


}
