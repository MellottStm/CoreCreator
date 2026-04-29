package com.smt.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmController {

    @FXML
    private Label titleLabel;
    @FXML private Label contentLabel;
    @FXML private Button okBtn;
    @FXML private Button cancelBtn;

    private boolean confirmed = false;

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setContent(String content) {
        contentLabel.setText(content);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void init(Stage stage) {

        okBtn.setOnAction(e -> {
            confirmed = true;
            stage.close();
        });

        cancelBtn.setOnAction(e -> {
            confirmed = false;
            stage.close();
        });
    }

}
