// File: src/main/java/org/zakaria/webbrowserfxx/MainApp.java
package org.zakaria.webbrowserfxx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("browser.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root, 1400, 900);

            // Apply CSS for better UI
            scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            primaryStage.setTitle("Enhanced JavaFX Web Browser");
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("Application started successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start the application.", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
