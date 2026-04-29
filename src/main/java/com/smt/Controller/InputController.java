package com.smt.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
public class InputController {

    @FXML private Label titleLabel;
    @FXML private TextField inputField;
    @FXML private Button okBtn;
    @FXML private Button cancelBtn;

    private String result = null;

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public String getResult() {
        return result;
    }

    public void setText (String text) {
        inputField.setText(text);
    }

    public void init(Stage stage) {

        okBtn.setOnAction(e -> {
            result = inputField.getText();
            stage.close();
        });

        cancelBtn.setOnAction(e -> {
            result = null;
            stage.close();
        });

        // 回车确认（像 IDEA）
        inputField.setOnAction(e -> okBtn.fire());
    }

    public interface InputDialogEvent {
        void showEvent (InputController controller);
    }

}
