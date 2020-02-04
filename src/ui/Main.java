package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.fxml.main.controllers.MainController;
import ui.fxml.main.controllers.TSNEPlotController;


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final Image logo = new Image("ui/icons/RNA-ScoopIcon.jpg");

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("fxml/main/main.fxml"));
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("fxml/main/console.fxml"));
        FXMLLoader isoformPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/isoformplot.fxml"));
        FXMLLoader tSNEPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/tsneplot.fxml"));
        BorderPane root = mainLoader.load();

        setUpMainWindow(mainLoader, consoleLoader, isoformPlotLoader, tSNEPlotLoader);
        setUpStage(primaryStage, root);
    }

    /**
     * Set up panels (isoform plot, t-SNE plot, console), controllers and main controller data
     */
    private void setUpMainWindow(FXMLLoader mainLoader, FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader, FXMLLoader tSNEPlotLoader) {
        MainController mainController = mainLoader.getController();
        mainController.initializeMain(consoleLoader, isoformPlotLoader, tSNEPlotLoader);
        TSNEPlotController tsnePlotController = tSNEPlotLoader.getController();
        tsnePlotController.initConsoleController(consoleLoader.getController());
    }

    private void setUpStage(Stage primaryStage, BorderPane root) {
        primaryStage.setTitle("RNA-Scoop");
        primaryStage.getIcons().add(logo);
        Rectangle2D screen = Screen.getPrimary().getBounds();
        primaryStage.setScene(new Scene(root, screen.getWidth() * SCALE_FACTOR, screen.getHeight() * SCALE_FACTOR));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
