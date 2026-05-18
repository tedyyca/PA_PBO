package com.toga;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/toga/view/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 960, 620);
        scene.getStylesheets().add(getClass().getResource("/com/toga/css/style.css").toExternalForm());
        stage.setTitle("Sistem Manajemen TOGA");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}