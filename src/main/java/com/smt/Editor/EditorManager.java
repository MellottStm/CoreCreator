package com.smt.Editor;

import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EditorManager {

    public static String TAG = "EditorManager";

    public final static Logger logger = Logger.getLogger(TAG);

    public final static Map<File, Tab> openTabs = new HashMap<>();           // 文件 → Tab

    public final static Map<Tab, MonacoFX> tabToEditor = new HashMap<>();   // Tab → 编辑器实例

    public final static Map<Tab, File> tabToFile = new HashMap<>();         // Tab → 文件（便于保存等）

    public final static Map<Tab, String> lastSavedContent = new HashMap<>();

    public final static Map<Tab, Long> tabToLastModifiedTime = new HashMap<>();

    public static String getLanguageByExtension(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".js")) return "javascript";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".ts")) return "Typescript";
        if (lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h")) return "c";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".xml") || lower.endsWith(".fxml")) return "xml";
        if (lower.endsWith(".json")) return "json";
        return "plaintext";
    }

    public static void saveTab (Tab tab) {
        if (tab == null) {
            return;
        }
        File file = tabToFile.get(tab);
        MonacoFX editor = tabToEditor.get(tab);
        if (file == null || editor == null) return;
        try {
            String content = editor.getEditor().getDocument().getText();  // 获取当前编辑器内容
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            tabToLastModifiedTime.put(tab, file.lastModified());
            lastSavedContent.put(tab, content);
        } catch (IOException e) {
            logger.warn("保存文件失败: " + file.getAbsolutePath(), e);
            // 可以弹 Alert 提示用户
        }
    }

    public static MonacoFX getMonacoFXFromFile (File file) {
        MonacoFX monacoFX = new MonacoFX();
        monacoFX.getEditor().setCurrentTheme("vs-dark");
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            monacoFX.getEditor().getDocument().setText(content);
            String lang = EditorManager.getLanguageByExtension(file.getName());
            monacoFX.getEditor().setCurrentLanguage(lang);
        } catch (IOException e) {
            logger.warn("打开文件异常:" + e);
        }
        return monacoFX;
    }

    public static void makeResizable(Stage stage, int margin, double minWidth, double minHeight) {

        Scene scene = stage.getScene();

        final Delta delta = new Delta();

        scene.setOnMouseMoved(event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();

            Cursor cursor = Cursor.DEFAULT;

            if (x < margin && y < margin) cursor = Cursor.NW_RESIZE;
            else if (x > width - margin && y < margin) cursor = Cursor.NE_RESIZE;
            else if (x < margin && y > height - margin) cursor = Cursor.SW_RESIZE;
            else if (x > width - margin && y > height - margin) cursor = Cursor.SE_RESIZE;
            else if (x < margin) cursor = Cursor.W_RESIZE;
            else if (x > width - margin) cursor = Cursor.E_RESIZE;
            else if (y < margin) cursor = Cursor.N_RESIZE;
            else if (y > height - margin) cursor = Cursor.S_RESIZE;

            scene.setCursor(cursor);
        });

        scene.setOnMousePressed(event -> {
            delta.startX = stage.getWidth();
            delta.startY = stage.getHeight();
            delta.startScreenX = event.getScreenX();
            delta.startScreenY = event.getScreenY();
            delta.startStageX = stage.getX();
            delta.startStageY = stage.getY();
            delta.cursor = scene.getCursor();
        });

        scene.setOnMouseDragged(event -> {
            Cursor cursor = delta.cursor;
            if (cursor == Cursor.DEFAULT) return;

            double dx = event.getScreenX() - delta.startScreenX;
            double dy = event.getScreenY() - delta.startScreenY;

            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                double newWidth = delta.startX + dx;
                if (newWidth >= minWidth) stage.setWidth(newWidth);
            }

            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                double newHeight = delta.startY + dy;
                if (newHeight >= minHeight) stage.setHeight(newHeight);
            }

            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                double newWidth = delta.startX - dx;
                if (newWidth >= minWidth) {
                    stage.setX(delta.startStageX + dx);
                    stage.setWidth(newWidth);
                }
            }

            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
                double newHeight = delta.startY - dy;
                if (newHeight >= minHeight) {
                    stage.setY(delta.startStageY + dy);
                    stage.setHeight(newHeight);
                }
            }
        });
    }

    private static class Delta {
        double startX, startY;
        double startScreenX, startScreenY;
        double startStageX, startStageY;
        Cursor cursor;
    }
    public static void checkEditorIsChange () {
        tabToEditor.forEach((tab,editor)->{
            File file = tabToFile.get(tab);
            if (file == null) return;
            long diskLastModified = file.lastModified();
            Long recordedLastModified = tabToLastModifiedTime.get(tab);
            if (recordedLastModified != null && diskLastModified > recordedLastModified) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("检测到文件外部修改: " + file.getAbsolutePath() + "，正在重新加载...");
                        try {
                            String newContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                            String currentEditorContent = editor.getEditor().getDocument().getText();
                            // 可选优化：只有内容真的不一样才刷新，防止光标跳动
                            if (!newContent.equals(currentEditorContent)) {
                                // 更新编辑器内容
                                editor.getEditor().getDocument().setText(newContent);
                                // 更新内部保存的内容记录
                                lastSavedContent.put(tab, newContent);
                            }
                            // 【关键】更新时间戳记录，防止重复加载
                            tabToLastModifiedTime.put(tab, diskLastModified);

                        } catch (IOException e) {
                            logger.warn("重新加载外部修改的文件失败: " + file.getAbsolutePath(), e);
                        }
                    }
                });
            } else {
                String savedContent = lastSavedContent.get(tab);
                String currentContent = editor.getEditor().getDocument().getText();
                if (savedContent != null && !savedContent.equals(currentContent)) {
                    logger.info("检测到文件:" + file.getAbsolutePath() + "发生更改,已保存!");
                    saveTab(tab);
                }
            }
        });
    }

}
