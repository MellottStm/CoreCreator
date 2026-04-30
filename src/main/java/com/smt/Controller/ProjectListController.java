package com.smt.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smt.Cache.CacheManager;
import com.smt.Cache.Configure;
import com.smt.Editor.EditorManager;
import com.smt.LangChain.LLMManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProjectListController implements Initializable {

    private static String TAG = "ProjectListController";

    public final static Logger logger = Logger.getLogger(TAG);


    @FXML private TextField searchField;

    @FXML private Button openBtn;

    @FXML private ListView<File> projectListView;

    @FXML private HBox titleBar;

    @FXML private Button minimizeButton;

    @FXML private Button maximizeButton;

    @FXML private Button closeButton;

    // 用于窗口拖动
    private double xOffset = 0;

    private double yOffset = 0;


    private final ObservableList<File> projectList = FXCollections.observableArrayList();

    private FilteredList<File> filteredList;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        EditorManager.makeResizable(this.stage,5,600,400);
        initData();
    }

    /**
     * 打开文件夹选择
     */
    private void openProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择项目");
        File dir = chooser.showDialog(stage);
        if (dir != null && dir.isDirectory()) {
            openMain(dir.getAbsolutePath());
            if (!projectList.contains(dir)) {
                projectList.add(dir);
                CacheManager.savePathList(projectList);
            }
        }
    }


    public void projectAdd (String path) {
        if (!projectList.contains(path)) {
            projectList.add(new File(path));
            CacheManager.savePathList(projectList);
        }
    }


    private void initData () {
        JSONObject loadJson = CacheManager.loadCache();
        if (loadJson != null) {
            if (loadJson.getJSONArray("pathList") != null) {
                JSONArray projectListJson = loadJson.getJSONArray("pathList");
                for (int i = 0;i < projectListJson.size();i++) {
                    projectList.add(new File(projectListJson.getJSONObject(i).getString("path")));
                }
            }
            if (loadJson.getString("project_path") != null && !loadJson.getString("project_path").isEmpty()) {
                File file = new File(loadJson.getString("project_path"));
                if (!file.exists()) {
                    Toast.makeText(stage,"\""+file.getAbsolutePath() + "\"The project path does not exist!",5000);
                    projectList.remove(file);
                    return;
                }
                openMain(loadJson.getString("project_path"));
            }
        }
    }


    /**
     * 打开选中的项目
     */
    private void openSelectedProject() {
        File selected = projectListView.getSelectionModel().getSelectedItem();
        if (!selected.exists()) {
            Toast.makeText(stage,"\""+selected.getAbsolutePath() + "\"The project path does not exist!",5000);
            projectList.remove(selected);
            return;
        }
        logger.info("打开项目: " + selected.getAbsolutePath());
        openMain(selected.getAbsolutePath());
    }



    private void openMain (String path) {
        try {
            FXMLLoader loader = new FXMLLoader(ProjectListController.class.getResource("/View/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1600, 900);
            MainController mainController = loader.getController();
            Stage mainStage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(ProjectListController.class.getResourceAsStream("/Img/logo.png")));
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("loading fail");
            }
            mainStage.initStyle(StageStyle.UNDECORATED);
            mainStage.setTitle("CoreCreator");
            mainStage.setScene(scene);
            mainStage.show();
            mainController.setStage(mainStage,stage,this);
            mainController.openProject(path);
            stage.hide();
        } catch (Exception e) {
            logger.info("打开Main异常:" + e);
        }
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 过滤器
        filteredList = new FilteredList<>(projectList, p -> true);
        projectListView.setItems(filteredList);

        // 搜索逻辑
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.toLowerCase();

            filteredList.setPredicate(file -> {
                if (keyword.isEmpty()) return true;

                String name = file.getName().toLowerCase();
                String path = file.getAbsolutePath().toLowerCase();

                return name.contains(keyword) || path.contains(keyword);
            });
        });
        projectListView.setCellFactory(list -> new ListCell<File>() {
            private final Label name = new Label();
            private final Label path = new Label();
            private final Button deleteBtn = new Button("✕");

            private final VBox textBox = new VBox(name, path);
            private final HBox topRow = new HBox();
            private final BorderPane root = new BorderPane();

            {
                // 样式
                name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                path.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

                deleteBtn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #aaaaaa; " +
                                "-fx-font-size: 12;"
                );

                // hover 效果（更像 IDEA）
                deleteBtn.setOnMouseEntered(e ->
                        deleteBtn.setStyle("-fx-text-fill: #ff5c5c; -fx-background-color: transparent;")
                );
                deleteBtn.setOnMouseExited(e ->
                        deleteBtn.setStyle("-fx-text-fill: #aaaaaa; -fx-background-color: transparent;")
                );

                // 布局
                topRow.getChildren().addAll(name, new Pane(), deleteBtn);
                HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

                textBox.setSpacing(3);

                root.setTop(topRow);
                root.setBottom(path);
                root.setStyle("-fx-padding: 8;");
            }
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    name.setText(item.getName());
                    path.setText(item.getAbsolutePath());
                    deleteBtn.setOnAction(e -> {
                        e.consume(); // 防止触发 ListView 选中
                        projectList.remove(item);
                        CacheManager.savePathList(projectList);
                    });

                    setGraphic(root);
                }
            }
        });

        // 打开项目按钮
        openBtn.setOnAction(e -> openProject());

        // 双击打开项目（像 IDEA）
        projectListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelectedProject();
            }
        });
        setupTitleBar();
        setupWindowButtons();
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

}
