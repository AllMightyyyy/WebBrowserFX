package org.zakaria.webbrowserfxx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("browser.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1000, 700);

        primaryStage.setTitle("Enhanced JavaFX Web Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
