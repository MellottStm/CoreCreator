package com.smt.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.log4j.Logger;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

public class DiffController implements Initializable {

    private static String TAG = "DiffController";

    public final static Logger logger = Logger.getLogger(TAG);

    @FXML private Button applyBtn;

    @FXML private Button reverseBtn;

    @FXML private CodeArea leftCodeArea;

    @FXML private CodeArea rightCodeArea;
    // 高亮样式定义
    private static final String RED_HIGHLIGHT = "deleted-line";   // 红色（删除）

    private static final String GREEN_HIGHLIGHT = "added-line";   // 绿色（新增）


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCodeAreas();
        setLeftText("public void setRightText(String text, int[] highlightLines) {\n" +
                "        rightCodeArea.replaceText(text != null ? text : \"\");\n" +
                "        if (highlightLines != null && highlightLines.length > 0) {\n" +
                "            highlightLines(rightCodeArea, highlightLines);\n" +
                "        }\n" +
                "    }", new int[]{2, 5, 10});   // 高亮第3、6、11行
        setRightText("public void setRightText(String text, int[] highlightLines) {\n" +
                "        rightCodeArea.replaceText(text != null ? text : \"\");\n" +
                "        if (highlightLines != null && highlightLines.length > 0) {\n" +
                "            highlightLines(rightCodeArea, highlightLines);\n" +
                "        }\n" +
                "    }", new int[]{3, 8});
    }

    private void setupCodeAreas() {
        // 设置行号
        leftCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(leftCodeArea));
        rightCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(rightCodeArea));

        // IDEA风格：白字黑底 + 字体设置
        String editorStyle = """
            -fx-font-family: 'Consolas', 'Courier New', 'Monaco', monospace;
            -fx-font-size: 13px;
            -fx-background-color: #1e1e1e;
            -fx-text-fill: white;
            -fx-padding: 5;
            """;

        leftCodeArea.setStyle(editorStyle);
        rightCodeArea.setStyle(editorStyle);

        // 内联注入高亮样式（红色和绿色）
        String highlightStyles = """
            .deleted-line {
                -rtfx-background-color: rgba(255, 85, 85, 0.35);
            }
            .added-line {
                -rtfx-background-color: rgba(80, 200, 120, 0.35);
            }
            """;

        leftCodeArea.getStylesheets().add("data:text/css," + highlightStyles);
        rightCodeArea.getStylesheets().add("data:text/css," + highlightStyles);
    }

    /**
     * 高亮指定行
     * @param codeArea     要高亮的 CodeArea
     * @param lineNumbers  要高亮的行号（从0开始）
     * @param isAdded      true=绿色（新增），false=红色（删除）
     */
    public void highlightLines(CodeArea codeArea, int[] lineNumbers, boolean isAdded) {
        if (lineNumbers == null || lineNumbers.length == 0) return;

        String styleClass = isAdded ? GREEN_HIGHLIGHT : RED_HIGHLIGHT;
        StyleSpans<Collection<String>> styleSpans = computeHighlightStyleSpans(
                codeArea.getText(), lineNumbers, styleClass);

        codeArea.setStyleSpans(0, styleSpans);
    }

    private StyleSpans<Collection<String>> computeHighlightStyleSpans(String text, int[] lineNumbers, String styleClass) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        String[] lines = text.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int length = line.length() + 1;  // +1 表示换行符

            boolean shouldHighlight = false;
            for (int targetLine : lineNumbers) {
                if (targetLine == i) {
                    shouldHighlight = true;
                    break;
                }
            }

            if (shouldHighlight) {
                spansBuilder.add(Collections.singleton(styleClass), length);
            } else {
                spansBuilder.add(Collections.emptyList(), length);
            }
        }

        return spansBuilder.create();
    }

    /** 左侧（Original）使用红色高亮 */
    public void setLeftText(String text, int[] highlightLines) {
        leftCodeArea.replaceText(text != null ? text : "");
        if (highlightLines != null && highlightLines.length > 0) {
            highlightLines(leftCodeArea, highlightLines, false); // false = 红色
        }
    }

    /** 右侧（Modified）使用绿色高亮 */
    public void setRightText(String text, int[] highlightLines) {
        rightCodeArea.replaceText(text != null ? text : "");
        if (highlightLines != null && highlightLines.length > 0) {
            highlightLines(rightCodeArea, highlightLines, true); // true = 绿色
        }
    }
}
