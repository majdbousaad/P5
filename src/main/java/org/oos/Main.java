package org.oos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.oos.controllers.MainView;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Privatbank");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String [] args){
        launch(args);
    }
}