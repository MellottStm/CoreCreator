package com.smt.Controller;

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

    @FXML private TreeView<File> fileTreeView;

    @FXML private TabPane editorContainer;

    private final Map<File, Tab> openTabs = new HashMap<>();           // 文件 → Tab

    private final Map<Tab, MonacoFX> tabToEditor = new HashMap<>();   // Tab → 编辑器实例

    private final Map<Tab, File> tabToFile = new HashMap<>();         // Tab → 文件（便于保存等）

    private final Map<Tab, String> lastSavedContent = new HashMap<>();

    private Tab currentTab;  // 当前选中的 Tab

    @FXML private SplitPane mainSplitPane;

    @FXML private TextArea chatArea;

    @FXML private TextArea promptField;

    @FXML private Button sendButton;

    private File currentRootDir;

    private Timer saveTimer;

    private Stage stage;

    public void setStage (Stage stage) {
        this.stage= stage;
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (saveTimer!=null) {
                    saveTimer.purge();
                    saveTimer.cancel();
                    saveTimer = null;
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
        //5. 自动保存文件
        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                openTabs.forEach((file,tab)->{
                    checkEditorIsChange();
                });
            }
        },0,800);
    }


    private void checkEditorIsChange () {
        tabToEditor.forEach((tab,editor)->{
            if (!lastSavedContent.get(tab).equals(editor.getEditor().getDocument().getText())) {
                logger.info("检测到文件:" + tabToFile.get(tab).getAbsolutePath() + "发生更改,已保存!");
                lastSavedContent.put(tab,editor.getEditor().getDocument().getText());
                saveTab(tab);
            }
        });
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

    private MonacoFX getMonacoFXFromFile (File file) {
        MonacoFX monacoFX = new MonacoFX();
        monacoFX.getEditor().setCurrentTheme("vs-dark");
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            monacoFX.getEditor().getDocument().setText(content);
            String lang = getLanguageByExtension(file.getName());
            monacoFX.getEditor().setCurrentLanguage(lang);
        } catch (IOException e) {
            logger.warn("打开文件异常:" + e);
        }
        return monacoFX;
    }

    /** 把文件内容加载到 Monaco 编辑器 */
    private void loadFileToEditor(File file) {
        if (openTabs.containsKey(file)) {
            Tab existingTab = openTabs.get(file);
            editorContainer.getSelectionModel().select(existingTab);
            return;
        }
        // 创建新 Tab
        Tab tab = new Tab(file.getName());
        tab.setTooltip(new Tooltip(file.getAbsolutePath()));  // 鼠标悬停显示完整路径
        // 为每个 Tab 创建独立的 MonacoFX 编辑器
        MonacoFX monacoFX = getMonacoFXFromFile(file);
        tab.setContent(monacoFX);
        // 记录映射关系
        openTabs.put(file, tab);
        tabToEditor.put(tab, monacoFX);
        tabToFile.put(tab, file);
        lastSavedContent.put(tab,monacoFX.getEditor().getDocument().getText());
        // 添加到 TabPane 并选中
        editorContainer.getTabs().add(tab);
        editorContainer.getSelectionModel().select(tab);
        // Tab 关闭事件：清理映射
        tab.setOnClosed(e -> {
            openTabs.remove(file);
            tabToEditor.remove(tab);
            tabToFile.remove(tab);
            lastSavedContent.remove(tab);
            logger.info("已经关闭tab!");
        });
        editorContainer.setVisible(true);
    }


    private void saveTab (Tab tab) {
        if (tab == null) {
            return;
        }
        File file = tabToFile.get(tab);
        MonacoFX editor = tabToEditor.get(tab);
        if (file == null || editor == null) return;
        try {
            String content = editor.getEditor().getDocument().getText();  // 获取当前编辑器内容
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("保存文件失败: " + file.getAbsolutePath(), e);
            // 可以弹 Alert 提示用户
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
