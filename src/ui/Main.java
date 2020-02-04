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


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final Image logo = new Image("ui/icons/RNA-ScoopIcon.jpg");

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("fxml/main/isoformplot.fxml"));
        FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("fxml/main/console.fxml"));
        FXMLLoader rightLoader = new FXMLLoader(getClass().getResource("fxml/main/tsneplot.fxml"));
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("fxml/main/main.fxml"));
        BorderPane root = mainLoader.load();
/*        root.setRight(rightLoader.load());
        root.setBottom(bottomLoader.load());
        root.setCenter(centerLoader.load());*/

        setUpControllers(mainLoader, bottomLoader, centerLoader, rightLoader);
        setUpWindow(primaryStage, root);
    }


    private void setUpControllers(FXMLLoader mainLoader, FXMLLoader bottomLoader, FXMLLoader centerLoader, FXMLLoader rightLoader) {
        MainController mainController = mainLoader.getController();
        mainController.initData(bottomLoader, centerLoader, rightLoader);
    }

    private void setUpWindow(Stage primaryStage, BorderPane root) {
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
