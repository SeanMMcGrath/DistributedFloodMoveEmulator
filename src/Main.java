import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FloodMoveEmulator
 *
 * @author - Sean McGrath
 */
public class Main extends Application {

    private static final String GUI = "GUI.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(GUI));
        Scene frame = new Scene(root);
        primaryStage.setTitle("Distributed Flood Puzzle Solver");
        primaryStage.setScene(frame);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
