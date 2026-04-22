package com.smt.Controller;

import com.smt.Editor.EditorManager;
import dev.langchain4j.model.openai.OpenAiChatModel;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class MainController implements Initializable {

    private static String TAG = "MainController";

    public final static Logger logger = Logger.getLogger(TAG);
    // FXML 注入的控件
    @FXML private MenuItem openFolderMenuItem;

    @FXML private MenuItem openSettingMenuItem;

    @FXML private TreeView<File> fileTreeView;

    @FXML private TabPane editorContainer;

    private final long delayCheckTime = 800;

    private Tab currentTab;  // 当前选中的 Tab

    @FXML private SplitPane mainSplitPane;

    @FXML private TextArea chatArea;

    @FXML private TextArea promptField;

    @FXML private Button sendButton;

    private File currentRootDir;

    private Timer saveTimer;

    private Stage stage;

    // --- 新增 AI 相关变量 ---

    public void setStage (Stage stage) {
        this.stage= stage;
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (saveTimer!=null) {
                    saveTimer.purge();
                    saveTimer.cancel();
                    saveTimer = null;
                    logger.info("已关闭Main窗口!");
                }
            }
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. 创建 Monaco 编辑器
        mainSplitPane.setVisible(false);
        editorContainer.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> {
                    if (newTab != null) {
                        currentTab = newTab;
                        // 这里可以做一些额外操作，比如更新状态栏等
                    }
                }
        );
        editorContainer.setVisible(false);
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
        fileTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {  // 双击
                TreeItem<File> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() != null && selectedItem.getValue().isFile()) {
                    loadFileToEditor(selectedItem.getValue());
                }
            }
        });
        // 4. 菜单 - 打开文件夹
        openFolderMenuItem.setOnAction(e -> openFolder());
        openFolderMenuItem.setOnAction(e-> openSetting());
        //5. 自动保存文件
        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EditorManager.checkEditorIsChange();
            }
        },0,delayCheckTime);
    }
    //打开设置对话框
    private void openSetting () {

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
        mainSplitPane.setVisible(true);
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
        if (EditorManager.openTabs.containsKey(file)) {
            Tab existingTab = EditorManager.openTabs.get(file);
            editorContainer.getSelectionModel().select(existingTab);
            return;
        }
        // 创建新 Tab
        Tab tab = new Tab(file.getName());
        tab.setTooltip(new Tooltip(file.getAbsolutePath()));  // 鼠标悬停显示完整路径
        // 为每个 Tab 创建独立的 MonacoFX 编辑器
        MonacoFX monacoFX = EditorManager.getMonacoFXFromFile(file);
        tab.setContent(monacoFX);
        // 记录映射关系
        EditorManager.openTabs.put(file, tab);
        EditorManager.tabToEditor.put(tab, monacoFX);
        EditorManager.tabToFile.put(tab, file);
        EditorManager.lastSavedContent.put(tab,monacoFX.getEditor().getDocument().getText());
        EditorManager.tabToLastModifiedTime.put(tab, file.lastModified());
        // 添加到 TabPane 并选中
        editorContainer.getTabs().add(tab);
        editorContainer.getSelectionModel().select(tab);
        // Tab 关闭事件：清理映射
        tab.setOnClosed(e -> {
            EditorManager.openTabs.remove(file);
            EditorManager.tabToEditor.remove(tab);
            EditorManager.tabToFile.remove(tab);
            EditorManager.lastSavedContent.remove(tab);
            logger.info("已经关闭tab!");
        });
        editorContainer.setVisible(true);
    }

}
