package com.smt;

import com.smt.Cache.CacheManager;
import com.smt.Controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/DiffView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root,1600,900);
        primaryStage.setTitle("IDEA风格代码编辑器");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


//    @Override
//    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/MainView.fxml"));
//        Parent root = loader.load();
//        MainController mainController = loader.getController();
//        mainController.setStage(stage);
//        Scene scene = new Scene(root, 1600, 900);
//        stage.setTitle("CoreCreator");
//        stage.setScene(scene);
//        stage.show();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}