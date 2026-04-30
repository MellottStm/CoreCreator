package com.smt.Controller;

import com.smt.Editor.EditorManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ProjectListController implements Initializable {

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
    }

    /**
     * 打开文件夹选择
     */
    private void openProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择项目");

        File dir = chooser.showDialog(stage);

        if (dir != null && dir.isDirectory()) {
            if (!projectList.contains(dir)) {
                projectList.add(dir);
            }
        }
    }

    /**
     * 打开选中的项目
     */
    private void openSelectedProject() {
        File selected = projectListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("打开项目: " + selected.getAbsolutePath());

            // 👉 这里你后面可以跳转 MainView
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 模拟初始数据（你可以替换为缓存）
        projectList.addAll(
                new File("F:/TestProject1"),
                new File("F:/DemoApp"),
                new File("F:/MyWorkspace/HelloWorld")
        );

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
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label name = new Label(item.getName());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

                    Label path = new Label(item.getAbsolutePath());
                    path.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

                    VBox box = new VBox(name, path);
                    box.setSpacing(3);

                    setGraphic(box);
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
