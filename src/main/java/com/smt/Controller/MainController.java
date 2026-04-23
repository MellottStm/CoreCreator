package com.smt.Controller;

import com.alibaba.fastjson.JSONObject;
import com.smt.Cache.CacheManager;
import com.smt.Cache.Configure;
import com.smt.Editor.ChatRenderer;
import com.smt.Editor.EditorManager;
import com.smt.LangChain.LLMManager;
import com.smt.Main;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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

    @FXML private WebView chatWebView;

    @FXML private TextArea promptField;

    @FXML private Button sendButton;

    private File currentRootDir;

    private Timer saveTimer;

    private Stage stage;

    private LLMManager llmManager;

    private List<ChatMessage> chatMessageList = new ArrayList<>();

    // 用于构建 HTML 内容的 StringBuilder
    private final StringBuilder chatHistoryHtml = new StringBuilder();

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
        WebEngine engine = chatWebView.getEngine();
        // 加载空白页面，设置基础样式
        engine.loadContent("<html><head><style>body{background:#1e1e1e; color:#d4d4d4; font-family: sans-serif; padding: 10px;}</style></head><body>欢迎使用 AI 助手</body></html>");
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
        openSettingMenuItem.setOnAction(e-> openSetting());
        //5. 自动保存文件
        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EditorManager.checkEditorIsChange();
            }
        },0,delayCheckTime);
        //6. AI助手发送按钮
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sendMsg();
            }
        });
        //7. 回车发送消息
        promptField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    sendMsg();
                }
            }
        });
        initData();
    }

    private void initData () {
        JSONObject loadJson = CacheManager.loadCache();
        if (loadJson != null) {
            Configure.API_KEY = loadJson.getString("API_KEY");
            Configure.LLM_URL = loadJson.getString("LLM_URL");
            Configure.LLM_NAME = loadJson.getString("LLM_NAME");
        }
    }


    private void sendMsg () {
        if (promptField.getText() == null || promptField.getText().isBlank() || promptField.getText().isEmpty()) {
            Toast.makeText(stage, "输入框不能为空!", 1000);
            return;
        }

        if (llmManager.createModel() == null) {
            Toast.makeText(stage, "未设置大模型参数!", 1000);
            return;
        }


        chatMessageList.add(UserMessage.from(promptField.getText()));
        appendMessage(promptField.getText(), true);
        promptField.clear();
        simulateAiResponse("java");


    }

    private void appendMessage(String text, boolean isUser) {
        // 使用工具类将文本转为 HTML
        String htmlFragment = ChatRenderer.render(text, isUser);

        // 提取 body 内容并追加到历史记录
        // 简单处理：直接追加 div，实际生产中可能需要更严谨的 HTML 解析
        // 这里我们直接操作 WebView 的 DOM 或者重新加载内容
        // 为了简单起见，我们重新加载整个内容字符串

        Platform.runLater(() -> {
            chatHistoryHtml.append(htmlFragment.replace("<html><head>.*</head><body>", "").replace("</body></html>", ""));
            String finalHtml = "<html><head>" + ChatRenderer.CSS_STYLE + "</head><body>" + chatHistoryHtml + "</body></html>";

            WebEngine engine = chatWebView.getEngine();
            engine.loadContent(finalHtml);

            // 自动滚动到底部
            engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        });
    }

    // --- 模拟 AI 回复 (用于演示代码高亮效果) ---
    private void simulateAiResponse(String input) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟网络延迟

                String responseText;
                if (input.contains("java")) {
                    responseText = "没问题，这是你要的 Java 快速排序代码：\n\n```java\npublic class QuickSort {\n    public static void sort(int[] arr, int low, int high) {\n        if (low < high) {\n            int pi = partition(arr, low, high);\n            sort(arr, low, pi - 1);\n            sort(arr, pi + 1, high);\n        }\n    }\n    // ... 省略部分代码\n}\n```";
                } else if (input.contains("python")) {
                    responseText = "这是 Python 的实现：\n\n```python\ndef quick_sort(arr):\n    if len(arr) <= 1:\n        return arr\n    pivot = arr[len(arr) // 2]\n    left = [x for x in arr if x < pivot]\n    middle = [x for x in arr if x == pivot]\n    right = [x for x in arr if x > pivot]\n    return quick_sort(left) + middle + quick_sort(right)\n```";
                } else if (input.contains("json")) {
                    responseText = "这是一个 JSON 配置示例：\n\n```json\n{\n  \"name\": \"MyApp\",\n  \"version\": \"1.0.0\",\n  \"dependencies\": {\n    \"langchain4j\": \"0.34.0\"\n  }\n}\n```";
                } else {
                    responseText = "我收到了你的消息：“" + input + "”。\n你可以问我关于 Java, Python, JSON 等代码的问题，我会高亮显示。";
                }

                appendMessage(responseText, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    //打开设置对话框
    private void openSetting ()  {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/SettingsView.fxml"));
            Parent root = loader.load();
            SettingsController settingsController = loader.getController();
            Stage settingsStage = new Stage();
            settingsController.setStage(settingsStage);
            Scene scene = new Scene(root, 480, 280);
            settingsStage.setTitle("Settings");
            settingsStage.setScene(scene);
            settingsStage.show();
        }catch (Exception e) {
            logger.warn("打开Setting异常:" + e);
        }
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
            llmManager = new LLMManager(selectedDir.getPath());
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
