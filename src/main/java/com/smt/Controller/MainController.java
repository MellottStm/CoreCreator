package com.smt.Controller;

import eu.mihosoft.monacofx.MonacoFX;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static String TAG = "MainController";

    public final static Logger logger = Logger.getLogger(TAG);

    // FXML 注入的控件
    @FXML
    private MenuItem openFolderMenuItem;
    @FXML private TreeView<File> fileTreeView;
    @FXML private StackPane editorContainer;
    @FXML private Label fileTitle;
    private MonacoFX monacoFX;
    private File currentRootDir;

    @FXML private TextArea chatArea;
    @FXML private TextArea promptField;
    @FXML private Button sendButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. 创建 Monaco 编辑器
        monacoFX = new MonacoFX();
        editorContainer.getChildren().add(monacoFX);
        monacoFX.getEditor().setCurrentLanguage("java");
        monacoFX.getEditor().setCurrentTheme("vs-dark");
        fileTitle.setVisible(false);
        // 2. 文件树配置（显示文件名 + 图标）
        fileTreeView.setCellFactory(tv -> new TreeCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    setGraphic(new Label(item.isDirectory() ? "📁" : "📄"));
                }
            }
        });

        // 3. 点击文件自动加载到编辑器
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null && newVal.getValue().isFile()) {
                loadFileToEditor(newVal.getValue());
            }
        });

        // 4. 菜单 - 打开文件夹
        openFolderMenuItem.setOnAction(e -> openFolder());
    }

    /** 打开文件夹对话框 */
    private void openFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择项目文件夹");
        Stage stage = (Stage) editorContainer.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);

        if (selectedDir != null && selectedDir.isDirectory()) {
            currentRootDir = selectedDir;
            buildFileTree(selectedDir);
        }
    }

    /** 构建完整文件树（文件夹优先，递归加载） */
    private void buildFileTree(File rootDir) {
        fileTitle.setVisible(true);
        TreeItem<File> rootItem = new TreeItem<>(rootDir);
        rootItem.setExpanded(true);
        addAllChildren(rootItem);

        fileTreeView.setRoot(rootItem);
        fileTreeView.setShowRoot(true);
    }

    private void addAllChildren(TreeItem<File> parent) {
        File dir = parent.getValue();
        if (!dir.isDirectory()) return;

        File[] children = dir.listFiles();
        if (children == null) return;

        // 排序：文件夹在前，文件在后，按名称排序
        java.util.Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File child : children) {
            TreeItem<File> childItem = new TreeItem<>(child);
            parent.getChildren().add(childItem);
            if (child.isDirectory()) {
                addAllChildren(childItem);   // 递归加载
            }
        }
    }

    /** 把文件内容加载到 Monaco 编辑器 */
    private void loadFileToEditor(File file) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            monacoFX.getEditor().getDocument().setText(content);

            // 根据文件后缀自动设置语言
            String lang = getLanguageByExtension(file.getName());
            monacoFX.getEditor().setCurrentLanguage(lang);


        } catch (IOException e) {

        }
    }

    private String getLanguageByExtension(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".js")) return "javascript";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h")) return "c";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".xml") || lower.endsWith(".fxml")) return "xml";
        if (lower.endsWith(".json")) return "json";
        return "plaintext";
    }

}
