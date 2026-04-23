package com.smt.Controller;

import com.smt.Cache.CacheManager;
import com.smt.Cache.Configure;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    private static String TAG = "SettingsController";

    public final static Logger logger = Logger.getLogger(TAG);

    @FXML private TextField apiKeyField;

    @FXML private TextField llmNameField;

    @FXML private TextField llmUrlField;

    @FXML private Button saveButton;

    @FXML private Button cancelButton;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                saveSettings();
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.close();
            }
        });

        apiKeyField.setText(((Configure.API_KEY == null) || (Configure.API_KEY.isEmpty())) ? "" : Configure.API_KEY);

        llmNameField.setText(((Configure.LLM_NAME == null) || (Configure.LLM_NAME.isEmpty())) ? "" : Configure.LLM_NAME);

        llmUrlField.setText(((Configure.LLM_URL == null) || (Configure.LLM_URL.isEmpty())) ? "" : Configure.LLM_URL);
    }


    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        String apiKey = apiKeyField.getText().trim();

        String modelName = llmNameField.getText().trim();

        String baseUrl = llmUrlField.getText().trim();

        // 保存到 LLMManager（推荐通过单例或静态方式保存）
        CacheManager.saveConfig(apiKey,modelName,baseUrl);

        Toast.makeText(stage, "大模型设置已保存！", 1500);

        stage.close();
    }

}
