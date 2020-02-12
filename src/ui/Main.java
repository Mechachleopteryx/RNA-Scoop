package ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ui.fxml.GeneSelectorController;
import ui.fxml.main.controllers.IsoformPlotController;
import ui.fxml.main.controllers.MainController;
import ui.fxml.main.controllers.TSNEPlotController;

import java.io.IOException;


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final float GENE_SELECTOR_SCALE_FACTOR = 0.35f;
    private static final Image logo = new Image("ui/icons/RNA-ScoopIcon.jpg");

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("fxml/main/main.fxml"));
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("fxml/main/console.fxml"));
        FXMLLoader isoformPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/isoformplot.fxml"));
        FXMLLoader tSNEPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/tsneplot.fxml"));
        FXMLLoader geneSelectorLoader = new FXMLLoader(getClass().getResource("/ui/fxml/geneselector.fxml"));
        BorderPane root = mainLoader.load();
        setUpGeneSelector(geneSelectorLoader);
        setUpMainWindow(mainLoader, consoleLoader, isoformPlotLoader, tSNEPlotLoader, geneSelectorLoader);
        setUpStage(primaryStage, root);
    }

    /**
     * Sets up gene selector window
     */
    private void setUpGeneSelector(FXMLLoader geneSelectorLoader) throws IOException {
        Parent root;
        root = geneSelectorLoader.load();
        Stage geneSelector = new Stage();
        geneSelector.setTitle("RNA-Scoop - Gene Selector");
        Rectangle2D screen = Screen.getPrimary().getBounds();
        geneSelector.setScene(new Scene(root, screen.getWidth() * GENE_SELECTOR_SCALE_FACTOR, screen.getHeight() * GENE_SELECTOR_SCALE_FACTOR));
        geneSelector.setOnCloseRequest(event -> {
            event.consume();
            geneSelector.hide();
        });
        GeneSelectorController geneSelectorController = geneSelectorLoader.getController();
        geneSelectorController.initWindow(geneSelector);
    }

    /**
     * Set up panels (isoform plot, t-SNE plot, console), controllers and main controller data
     */
    private void setUpMainWindow(FXMLLoader mainLoader, FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader,
                                 FXMLLoader tSNEPlotLoader, FXMLLoader geneSelectorLoader) throws IOException {
        MainController mainController = mainLoader.getController();
        mainController.initializeMain(consoleLoader, isoformPlotLoader, tSNEPlotLoader, geneSelectorLoader);
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
