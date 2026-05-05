package com.smt.Editor;

import eu.mihosoft.monacofx.MonacoFX;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.*;
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

    public final static Map<Tab, TextArea> tabToTextReadOnly = new HashMap<>(); //Tab -> 只读文件

    public final static Map<Tab, File> tabToFile = new HashMap<>();         // Tab → 文件（便于保存等）

    public final static Map<Tab, String> lastSavedContent = new HashMap<>();

    public final static Map<Tab, Long> tabToLastModifiedTime = new HashMap<>();

    public static void releaseMap () {
        openTabs.clear();
        tabToEditor.clear();
        tabToTextReadOnly.clear();
        tabToFile.clear();
        lastSavedContent.clear();
        tabToLastModifiedTime.clear();
    }

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
         if (!(EditorManager.isPdf(file)||EditorManager.isDocx(file))) {
            MonacoFX editor = tabToEditor.get(tab);
            if (file == null || editor == null) return;
            try {
                String content = editor.getEditor().getDocument().getText();  // 获取当前编辑器内容
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
                tabToLastModifiedTime.put(tab, file.lastModified());
                lastSavedContent.put(tab, content);
            } catch (IOException e) {
                logger.warn("保存文件失败: " + file.getAbsolutePath(), e);
            }
        }
    }

    public static MonacoFX getMonacoFXFromFile (File file) {
        MonacoFX monacoFX = null;
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            monacoFX = new MonacoFX();
            monacoFX.getEditor().setCurrentTheme("vs-dark");
            monacoFX.getEditor().getDocument().setText(content);
            String lang = EditorManager.getLanguageByExtension(file.getName());
            monacoFX.getEditor().setCurrentLanguage(lang);
        } catch (IOException e) {
            logger.warn("打开文件异常:" + e);
        }
        return monacoFX;
    }

    public static String readPdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readDocx(File file) {
        StringBuilder content = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            doc.getParagraphs().forEach(p -> {
                content.append(p.getText()).append("\n");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }


    public static boolean isPdf(File file) {
        try (InputStream is = new FileInputStream(file)) {
            byte[] header = new byte[4];
            if (is.read(header) != -1) {
                String str = new String(header);
                return str.equals("%PDF");
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isDocx(File file) {
        // DOCX 本质是 ZIP 文件（PK开头）
        try (InputStream is = new FileInputStream(file)) {
            byte[] header = new byte[2];
            if (is.read(header) != -1) {
                return header[0] == 'P' && header[1] == 'K';
            }
        } catch (Exception ignored) {}
        return false;
    }

    //创建并且写入docx
    public static void writeDocx(File file, String text) {
        try (XWPFDocument doc = new XWPFDocument()) {

            for (String line : text.split("\n")) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(line);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                doc.write(out);
            }

        } catch (Exception e) {
            logger.info("创建Docx文件失败:" + e);
        }
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

        scene.setOnMouseReleased(e -> {
            scene.setCursor(Cursor.DEFAULT);
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

    public static TextArea createReadOnlyTextArea(String content) {
        TextArea textArea = new TextArea();
        textArea.setStyle(
                "-fx-control-inner-background: #2b2b2b;" +
                        "-fx-text-fill: white;" +
                        "-fx-highlight-fill: #214283;" +
                        "-fx-highlight-text-fill: white;" +
                        "-fx-font-family: 'Consolas';" +
                        "-fx-font-size: 13px;" +
                        "-fx-border-color: #3c3f41;" +
                        "-fx-border-radius: 3;" +
                        "-fx-background-radius: 3;"
        );
        textArea.setText(content);
        textArea.setEditable(false);
        return textArea;
    }

    public static Tab createTab(TabPane editorContainer, File file, Node content, boolean readOnly) {
        Tab tab = new Tab(file.getName() + (readOnly ? "(Read-Only)" : ""));
        tab.setTooltip(new Tooltip(file.getAbsolutePath()));
        tab.setContent(content);
        editorContainer.getTabs().add(tab);
        editorContainer.getSelectionModel().select(tab);
        return tab;
    }

    public static void registerTab(File file, Tab tab, Object editor, boolean readOnly) {

        openTabs.put(file, tab);
        tabToFile.put(tab, file);

        String content;

        if (readOnly) {
            TextArea textArea = (TextArea) editor;
            tabToTextReadOnly.put(tab, textArea);
            content = textArea.getText();
        } else {
            MonacoFX monaco = (MonacoFX) editor;
            EditorManager.tabToEditor.put(tab, monaco);
            content = monaco.getEditor().getDocument().getText();
        }

        lastSavedContent.put(tab, content);
        tabToLastModifiedTime.put(tab, file.lastModified());

        // ✅ 统一关闭逻辑（只写一次）
        tab.setOnClosed(e -> {
            openTabs.remove(file);
            tabToEditor.remove(tab);
            tabToTextReadOnly.remove(tab);
            tabToFile.remove(tab);
            lastSavedContent.remove(tab);
            tabToLastModifiedTime.remove(tab);
            logger.info("已经关闭tab!");
        });
    }


    private static void processTab(Tab tab, EditorAdapter adapter) {

        File file = tabToFile.get(tab);
        if (file == null) return;

        long diskLastModified = file.lastModified();
        Long recordedLastModified = tabToLastModifiedTime.get(tab);

        // 外部修改
        if (recordedLastModified != null && diskLastModified > recordedLastModified) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logger.info("检测到文件外部修改: " + file.getAbsolutePath());
                    try {
                        String newContent = "";

                        if (EditorManager.isPdf(file)) {
                            newContent = EditorManager.readPdf(file);
                        } else if (EditorManager.isDocx(file)) {
                            newContent = EditorManager.readDocx(file);
                        } else {
                            newContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                        }
                        String currentContent = adapter.getText();

                        if (!newContent.equals(currentContent)) {
                            adapter.setText(newContent);
                            lastSavedContent.put(tab, newContent);
                        }
                        tabToLastModifiedTime.put(tab, diskLastModified);
                    } catch (IOException e) {
                        logger.warn("重新加载失败: " + file.getAbsolutePath(), e);
                    }
                }
            });
            return;
        }
        // 本地修改自动保存
        String savedContent = lastSavedContent.get(tab);
        String currentContent = adapter.getText();
        if (savedContent != null && !savedContent.equals(currentContent)) {
            logger.info("检测到文件变更: " + file.getAbsolutePath() + "，自动保存");
            saveTab(tab);
        }
    }


    public static void checkEditorIsChange () {
        tabToTextReadOnly.forEach((tab, textArea) -> {
            processTab(tab, new TextAreaAdapter(textArea));
        });
        tabToEditor.forEach((tab, editor) -> {
            processTab(tab, new MonacoAdapter(editor));
        });
    }

}
