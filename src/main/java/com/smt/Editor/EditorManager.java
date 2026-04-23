package com.smt.Editor;

import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

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
