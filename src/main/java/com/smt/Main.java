package com.smt;


import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // 左侧：Monaco 编辑器
        // create a new monaco editor node
        MonacoFX monacoFX = new MonacoFX();
        StackPane root = new StackPane(monacoFX);

        // set initial text
        monacoFX.getEditor().getDocument().setText(
                "#include <stdio.h>\n" +
                        "int main() {\n" +
                        "   // printf() displays the string inside quotation\n" +
                        "   printf(\"Hello, World!\");\n" +
                        "   return 0;\n" +
                        "}");

        // use a predefined language like 'c'
        monacoFX.getEditor().setCurrentLanguage("c");
        monacoFX.getEditor().setCurrentTheme("vs-dark");

        // the usual scene & stage setup
        Scene scene = new Scene(root, 800,600);
        stage.setTitle("MonacoFX Demo (running on JDK " + System.getProperty("java.version") + ")");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}