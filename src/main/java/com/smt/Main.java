package com.smt;

import com.smt.Controller.MainController;
import com.smt.Controller.ProjectListController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class Main extends Application {
//    @Override
//    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/MainView.fxml"));
//        Parent root = loader.load();
//        Scene scene = new Scene(root, 1600, 900);
//        MainController mainController = loader.getController();
//        try {
//            Image icon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/Img/logo.png")));
//            stage.getIcons().add(icon);
//        } catch (Exception e) {
//            System.out.println("loading fail");
//        }
//        stage.initStyle(StageStyle.UNDECORATED);
//        stage.setTitle("CoreCreator");
//        stage.setScene(scene);
//        stage.show();
//        mainController.setStage(stage);
//    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/ProjectListView.fxml"));
        Parent root = loader.load();
        ProjectListController controller = loader.getController();
        try {
            Image icon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/Img/logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("loading fail");
        }
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("CoreCreator");
        stage.setScene(new Scene(root, 800, 700));
        controller.setStage(stage);
        stage.show();
    }
}