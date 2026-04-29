package com.smt.Controller;

import com.alibaba.fastjson.JSONObject;
import com.smt.Cache.CacheManager;
import com.smt.Cache.Configure;
import com.smt.Editor.ChatRenderer;
import com.smt.Editor.EditorManager;
import com.smt.LangChain.Bean.ResultBean;
import com.smt.LangChain.LLMManager;
import com.smt.MCP.FilesManager;
import com.smt.Main;
import com.smt.Thread.ThreadManager;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @FXML private HBox titleBar;

    @FXML private Button minimizeButton;

    @FXML private Button maximizeButton;

    @FXML private Button closeButton;

    // 用于窗口拖动
    private double xOffset = 0;
    private double yOffset = 0;

    private Timer saveTimer;

    // 存储当前所有文件和文件夹的路径快照
    private Set<String> filePathSnapshot = new HashSet<>();
    // 文件监控定时器
    private Timer fileWatchTimer;
    // 扫描锁，防止并发扫描
    private boolean isScanning = false;

    private Stage stage;

    private LLMManager llmManager;

    private List<ChatMessage> chatMessageList = new ArrayList<>();

    // 用于构建 HTML 内容的 StringBuilder
    private final StringBuilder chatHistoryHtml = new StringBuilder();

    // 标记 WebView 是否已经初始化过 HTML 结构
    private boolean isWebViewInitialized = false;

    // 新增：用于存储展开状态的路径集合
    private Set<String> expandedPaths = new HashSet<>();

    public void setStage (Stage stage) {
        this.stage= stage;
        EditorManager.makeResizable(stage,5,800,600);
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (saveTimer!=null) {
                    saveTimer.purge();
                    saveTimer.cancel();
                    saveTimer = null;
                }
                if (fileWatchTimer != null) {
                    fileWatchTimer.purge();
                    fileWatchTimer.cancel();
                    fileWatchTimer = null;
                }
                logger.info("已关闭Main窗口!");
            }
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. 创建 Monaco 编辑器
        WebEngine engine = chatWebView.getEngine();
        // 加载空白页面，设置基础样式
        engine.loadContent("<html><head><style>body{background:#1e1e1e; color:#d4d4d4; font-family: sans-serif; padding: 10px;}</style></head><body>Welcome!</body></html>");
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
        setupTitleBar();
        setupWindowButtons();
        initData();
    }

    private void initData () {
        JSONObject loadJson = CacheManager.loadCache();
        if (loadJson != null) {
            Configure.API_KEY = loadJson.getString("API_KEY");
            Configure.LLM_URL = loadJson.getString("LLM_URL");
            Configure.LLM_NAME = loadJson.getString("LLM_NAME");
            if (loadJson.getString("save_path") != null && !loadJson.getString("save_path").isEmpty()) {
                File selectedDir = new File(loadJson.getString("save_path"));
                if (selectedDir.isDirectory()) {
                    buildFileTree(selectedDir);
                    llmManager = new LLMManager(selectedDir.getPath());
                    startFileWatcher(selectedDir);
                }
            }
        }


    }

    private void startFileWatcher(File rootDir) {
        if (fileWatchTimer != null) {
            fileWatchTimer.purge();
            fileWatchTimer.cancel();
            fileWatchTimer = null;
        }
        // 1. 初始化快照：记录当前所有的文件路径
        filePathSnapshot.clear();
        scanFilePaths(rootDir, filePathSnapshot);

        fileWatchTimer = new Timer("FileWatchTimer");
        // 每 2 秒检查一次
        fileWatchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isScanning) return; // 如果上次扫描还没结束，跳过本次

                try {
                    isScanning = true;
                    if (rootDir.exists() && rootDir.isDirectory()) {
                        checkForChanges(rootDir);
                    }
                } catch (Exception e) {
                    logger.warn("检测文件树更新异常:" + e);
                } finally {
                    isScanning = false;
                }
            }
        }, 0, delayCheckTime);
    }


    /**
     * 检查是否有新增或删除
     */
    private void checkForChanges(File rootDir) {
        // 1. 获取当前磁盘上的最新文件列表
        Set<String> currentPaths = new HashSet<>();
        scanFilePaths(rootDir, currentPaths);

        boolean hasChanged = false;

        // 2. 检测新增：当前有，但快照里没有
        for (String path : currentPaths) {
            if (!filePathSnapshot.contains(path)) {
                System.out.println("检测到新增: " + path);
                hasChanged = true;
            }
        }

        // 3. 检测删除：快照里有，但当前没有了
        for (String path : filePathSnapshot) {
            if (!currentPaths.contains(path)) {
                System.out.println("检测到删除: " + path);
                hasChanged = true;
            }
        }

        // 4. 如果有变动，更新快照并刷新 UI
        if (hasChanged) {
            filePathSnapshot = currentPaths; // 更新快照

            // 刷新目录树 UI
            Platform.runLater(() -> {
                System.out.println("刷新目录树...");
                buildFileTree(rootDir);
                // 如果需要保持展开状态，可以在 buildFileTree 后手动展开根节点
                // fileTreeView.setExpanded(true);
            });
        }
    }


    /**
     * 递归扫描文件路径并存入 Set
     */
    private void scanFilePaths(File dir, Set<String> paths) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            paths.add(file.getAbsolutePath());
            // 如果是目录，递归进去
            if (file.isDirectory()) {
                scanFilePaths(file, paths);
            }
        }
    }

    private void sendMsg () {
        initWebView();
        if (promptField.getText() == null || promptField.getText().isBlank() || promptField.getText().isEmpty()) {
            Toast.makeText(stage, "输入框不能为空!", 1000);
            return;
        }
        if (llmManager.createModel() == null) {
            Toast.makeText(stage, "未设置大模型参数!", 1000);
            return;
        }
        chatMessageList.add(UserMessage.from(promptField.getText()));
        appendUserMessage(promptField.getText());
        promptField.clear();
        promptField.setPromptText("Waiting Ai response...");
        promptField.setEditable(false);
        sendButton.setDisable(true);
        initAiMessage();
        ThreadManager.setThreadToPool(new Runnable() {
            @Override
            public void run() {
                llmManager.requestLLMStream(chatMessageList, new LLMManager.RequestCallBack() {
                    @Override
                    public void streamResult(String result) {
                        logger.info("大模型流式返回的结果：" + result);
                        updateAiMessage(result);
                    }

                    @Override
                    public void finalResult(String result) {
                        logger.info("大模型流式返回的最终结果：" + result);
                        updateAiMessage(result);
                        chatMessageList.add(AiMessage.from(result));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                promptField.setPromptText("Ask something...");
                                promptField.setEditable(true);
                                sendButton.setDisable(false);
                            }
                        });
                    }
                }).whenComplete((resultBeanList, throwable) -> {
                    if (resultBeanList != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showDiff(resultBeanList);
                            }
                        });
                    }
                });
            }
        });
    }


    private void setupTitleBar() {
        titleBar.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            if (stage != null) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // 可选：双击标题栏进行最大化/还原
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize();
            }
        });
    }

    /**
     * 设置窗口控制按钮事件
     */
    private void setupWindowButtons() {
        // 最小化
        minimizeButton.setOnAction(event -> {
            if (stage != null) {
                stage.setIconified(true);
            }
        });

        // 最大化 / 还原
        maximizeButton.setOnAction(event -> toggleMaximize());

        // 关闭
        closeButton.setOnAction(event -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    /**
     * 切换最大化 / 还原状态，并更新按钮图标
     */
    private void toggleMaximize() {
        if (stage == null) return;

        boolean isMaximized = stage.isMaximized();
        stage.setMaximized(!isMaximized);

        // 更新按钮图标
        if (stage.isMaximized()) {
            maximizeButton.setText("🗗");   // 还原图标
        } else {
            maximizeButton.setText("🗖");   // 最大化图标
        }
    }


    private void showDiff (List<ResultBean> resultBeanList) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/DiffView.fxml"));
            Parent root = loader.load();
            DiffController diffController = loader.getController();
            Stage diffStage = new Stage();
            Scene scene = new Scene(root, 1600, 900);
            diffStage.setScene(scene);
            try {
                Image icon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/Img/logo.png")));
                diffStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("loading fail");
            }
            diffController.setStage(diffStage);
            diffStage.initStyle(StageStyle.UNDECORATED);
            diffStage.setTitle("CoreCreator");
            diffStage.show();
            boolean shouldShow = true;
            File file;
            for (ResultBean bean:resultBeanList) {
                if (bean.path==null || bean.path.isEmpty()) {
                    shouldShow = false;
                    break;
                }
                switch (bean.operationType) {
                    case add:
                        diffController.addDiffFile(bean.path,"",bean.content.toString());
                        break;
                    case update:
                        file = new File(bean.path);
                        if (file.exists()) {
                            String content = Files.readString(Paths.get(bean.path));
                            diffController.addDiffFile(bean.path,content, bean.content.toString());
                        }
                        break;
                    case del:
                        file = new File(bean.path);
                        if (file.exists()) {
                            String content = Files.readString(Paths.get(bean.path));
                            diffController.addDiffFile(bean.path,content, "");
                        }
                        break;
                }
            }
            if (shouldShow) {
                diffStage.show();
            }
            diffController.setEvent(new DiffController.Event() {
                @Override
                public void applyEvent() {
                    for (ResultBean resultBean : resultBeanList) {
                        FilesManager.managerProject(resultBean.path, resultBean.content.toString(), resultBean.operationType);
                    }
                    diffStage.close();
                }
            });
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * 初始化 WebView 的基础 HTML 结构（只调用一次）
     */
    private void initWebView() {
        if (isWebViewInitialized) return;

        WebEngine engine = chatWebView.getEngine();
        // 加载一个空的容器
        String initialHtml = ChatRenderer.wrapHtml("");
        engine.loadContent(initialHtml);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // 页面加载完成后滚动到底部
                engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            }
        });
        isWebViewInitialized = true;
    }


    private void appendUserMessage(String text) {
        String messageHtml = ChatRenderer.render(text, true);

        // 更新内存中的历史
        chatHistoryHtml.append(messageHtml);

        // 使用 JS 将新消息追加到 body 中，而不是 reload 整个页面
        Platform.runLater(() -> {
            WebEngine engine = chatWebView.getEngine();
            String escapedHtml = messageHtml.replace("'", "\\'").replace("\n", "");
            String script = "var div = document.createElement('div'); div.innerHTML = '" + escapedHtml + "'; document.body.appendChild(div.firstElementChild); window.scrollTo(0, document.body.scrollHeight);";
            engine.executeScript(script);
        });
    }


    /**
     * 2. 初始化 AI 消息（创建空的气泡占位）
     * 必须在 AI 开始流式输出前调用一次
     */
    private void initAiMessage() {
        Platform.runLater(() -> {
            // 生成一个空的 AI 消息 HTML 结构
            String emptyMessageHtml = ChatRenderer.render("Please wait...", false); // 渲染空字符串

            // 追加到历史记录
            chatHistoryHtml.append(emptyMessageHtml);

            // 使用 JS 追加到 DOM，而不是 reload 整个页面
            WebEngine engine = chatWebView.getEngine();
            String escapedHtml = emptyMessageHtml.replace("'", "\\'").replace("\n", "");
            String script = "var div = document.createElement('div'); div.innerHTML = '" + escapedHtml + "'; document.body.appendChild(div.firstElementChild); window.scrollTo(0, document.body.scrollHeight);";
            engine.executeScript(script);
        });
    }

    /**
     * 3. 更新 AI 消息（流式核心：通过 JS 修改 DOM）
     * @param currentText AI 当前累积的完整文本（包含 Markdown 语法）
     */
    private void updateAiMessage(String currentText) {
        Platform.runLater(() -> {
            WebEngine engine = chatWebView.getEngine();
            // 利用 ChatRenderer 生成用于更新内部 HTML 的 JS 脚本
            String script = ChatRenderer.generateUpdateScript(currentText);
            // 执行脚本
            engine.executeScript(script);
            // 保持滚动条在最底部
            engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        });
    }



    //打开设置对话框
    private void openSetting ()  {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/SettingsView.fxml"));
            Parent root = loader.load();
            SettingsController settingsController = loader.getController();
            Stage settingsStage = new Stage();
            Scene scene = new Scene(root, 480, 280);
            try {
                Image icon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/Img/logo.png")));
                settingsStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("loading fail");
            }
            settingsStage.setScene(scene);
            settingsController.setStage(settingsStage);
            settingsStage.initStyle(StageStyle.UNDECORATED);
            settingsStage.setTitle("Settings");
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
            CacheManager.saveProjectPath(selectedDir.getPath());
            buildFileTree(selectedDir);
            llmManager = new LLMManager(selectedDir.getPath());
            startFileWatcher(selectedDir);
        }
    }

    /** 构建完整文件树（文件夹优先，递归加载） */
    private void buildFileTree(File rootDir) {
        mainSplitPane.setVisible(true);
        // 1. 在重建前，保存当前所有展开节点的路径
        saveExpandedState(fileTreeView.getRoot());
        TreeItem<File> rootItem = new TreeItem<>(rootDir);
        rootItem.setExpanded(true); // 根节点默认展开
        addAllChildren(rootItem);
        fileTreeView.setRoot(rootItem);
        fileTreeView.setShowRoot(true);
        // 2. 重建后，恢复展开状态
        restoreExpandedState(rootItem);
    }


    /**
     * 递归保存展开的路径
     */
    private void saveExpandedState(TreeItem<File> item) {
        if (item == null) return;
        if (item.isExpanded()) {
            expandedPaths.add(item.getValue().getAbsolutePath());
        }
        for (TreeItem<File> child : item.getChildren()) {
            saveExpandedState(child);
        }
    }

    /**
     * 递归恢复展开状态
     */
    private void restoreExpandedState(TreeItem<File> item) {
        if (item == null) return;

        String path = item.getValue().getAbsolutePath();
        if (expandedPaths.contains(path)) {
            item.setExpanded(true);
        }

        // 注意：我们需要先递归处理子节点，确保子节点也被正确设置
        // 但 addAllChildren 是懒加载的，所以我们需要确保子节点已加载
        // 如果子节点还没加载（比如之前没展开过），这里不需要处理

        for (TreeItem<File> child : item.getChildren()) {
            restoreExpandedState(child);
        }
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
