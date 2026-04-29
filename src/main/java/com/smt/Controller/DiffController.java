package com.smt.Controller;
import com.smt.Editor.Diff;
import com.smt.Editor.DiffFile;
import com.smt.Editor.DiffManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.apache.log4j.Logger;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.net.URL;
import java.util.*;

public class DiffController implements Initializable {

    private static String TAG = "DiffController";

    public final static Logger logger = Logger.getLogger(TAG);

    @FXML private Button applyBtn;

    @FXML private Button reverseBtn;

    @FXML private CodeArea leftCodeArea;

    @FXML private CodeArea rightCodeArea;

    @FXML private ListView<String> fileListView;

    private final Map<String, DiffFile> diffFiles = new LinkedHashMap<>();

    // 高亮样式定义
    private static final String RED_HIGHLIGHT = "deleted-line";   // 红色（删除）

    private static final String GREEN_HIGHLIGHT = "added-line";   // 绿色（新增）

    public enum HighType {
        RED,
        GREEN
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCodeAreas();
        setupFileListView();
//        loadTestData();
        // 默认选中第一个文件
        if (!fileListView.getItems().isEmpty()) {
            fileListView.getSelectionModel().select(0);
        }
        applyBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });
        reverseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });
    }

    /**
     * 外部调用此方法添加一个 diff 文件
     */
    public void addDiffFile(String fileName, String original, String modified) {

        List<String> originalList = Arrays.asList(original.split("\\r?\\n", -1));
        List<String> modifiedList = Arrays.asList(modified.split("\\r?\\n", -1));


        List<Diff> diffResult = DiffManager.compareTexts(originalList, modifiedList);

        // 添加到列表并默认选中
        if (!fileListView.getItems().contains(fileName)) {
            fileListView.getItems().add(fileName);
        }
        fileListView.getSelectionModel().select(fileName); // 选中当前添加的文件

    }

    private void setupFileListView() {
        fileListView.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: #cccccc;");

        fileListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDiff(newVal);
            }
        });
    }

    private void showDiff(String fileName) {
        DiffFile diff = diffFiles.get(fileName);
        if (diff == null) return;

        // 注意：这里传入的是颜色类型列表，而不是行号
        setLeftText(diff.originalText, diff.leftHighlightTypes);
        setRightText(diff.modifiedText, diff.rightHighlightTypes);
    }


    private void setupCodeAreas() {
        // 设置行号
        leftCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(leftCodeArea));
        rightCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(rightCodeArea));

        // IDEA风格：白字黑底 + 字体设置
        String editorStyle = """
            -fx-font-family: 'Consolas', 'Courier New', 'Monaco', monospace;
            -fx-font-size: 13px;
            """;

        // 应用基本样式
        leftCodeArea.setStyle(editorStyle);
        rightCodeArea.setStyle(editorStyle);

        // 内联注入高亮样式（红色和绿色）及文本颜色样式
        String highlightStyles = """
            .code-area {
                -fx-background-color: #1e1e1e;
            }
            
            .text, .paragraph-text {
                -fx-fill: white;
            }
            
            .paragraph-box {
                -fx-background-color: #1e1e1e;
            }
           
            .lineno {
                -fx-background-color: #1e1e1e;
                -fx-text-fill: gray;
                -fx-font-family: 'Consolas', 'Courier New', 'Monaco', monospace;
                -fx-font-size: 13px;
                -fx-alignment: center-right;
                -fx-padding: 0 5 0 5;
                -fx-pref-width: 50;
            }
            
            .deleted-line {
                -rtfx-background-color: rgba(255, 85, 85, 0.35);
                -fx-fill: white !important;
            }
            
            .added-line {
                -rtfx-background-color: rgba(80, 200, 120, 0.35);
                -fx-fill: white !important;
            }
            
            .lineno {
                -fx-text-fill: #888888;
            }
            """;

        leftCodeArea.getStylesheets().add("data:text/css," + highlightStyles);
        rightCodeArea.getStylesheets().add("data:text/css," + highlightStyles);
    }

    /**
     * 高亮指定行
     * @param codeArea     要高亮的 CodeArea
     */
    public void highlightLines(CodeArea codeArea,  List<HighType> lineTypes) {
        if (lineTypes == null || lineTypes.isEmpty()) return;

        StyleSpans<Collection<String>> styleSpans = computeHighlightStyleSpans(
                codeArea.getText(), lineTypes
        );
        codeArea.setStyleSpans(0, styleSpans);
    }

    private StyleSpans<Collection<String>> computeHighlightStyleSpans(String text, List<HighType> lineTypes) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        String[] lines = text.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 注意：这里 length 至少为 1，防止空行塌陷
            int length = Math.max(1, line.length() + 1);

            // 获取这一行应该是什么颜色
            HighType type = (i < lineTypes.size()) ? lineTypes.get(i) : null;

            if (type == HighType.RED) {
                spansBuilder.add(Collections.singleton(RED_HIGHLIGHT), length);
            } else if (type == HighType.GREEN) {
                spansBuilder.add(Collections.singleton(GREEN_HIGHLIGHT), length);
            } else {
                // 默认情况（包括 EQUAL 和空行）
                spansBuilder.add(Collections.emptyList(), length);
            }
        }
        return spansBuilder.create();
    }

    /** 左侧（Original）使用红色高亮 */
    public void setLeftText(String text, List<HighType> highlightTypes) {
        leftCodeArea.replaceText(text != null ? text : "");
        if (highlightTypes != null && !highlightTypes.isEmpty()) {
            highlightLines(leftCodeArea, highlightTypes);
        }
    }

    /** 右侧（Modified）使用绿色高亮 */
    public void setRightText(String text, List<HighType> highlightTypes) {
        rightCodeArea.replaceText(text != null ? text : "");
        if (highlightTypes != null && !highlightTypes.isEmpty()) {
            highlightLines(rightCodeArea, highlightTypes);
        }
    }
}
