package com.smt.Editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRenderer {

    // 优化后的 CSS：使用 Flexbox 实现左右对齐，添加气泡样式
    public static final String CSS_STYLE = """
        <style>
            body { 
                font-family: 'Segoe UI', sans-serif; 
                color: #d4d4d4; 
                background-color: #1e1e1e; 
                margin: 0; 
                padding: 15px; 
                line-height: 1.6; 
            }
            
            /* 通用消息容器：使用 Flex 布局 */
            .message-row { 
                display: flex; 
                margin-bottom: 15px; 
                width: 100%; 
            }
            
            /* AI 消息：靠左 */
            .message-row.ai { justify-content: flex-start; }
            
            /* 用户消息：靠右 */
            .message-row.user { justify-content: flex-end; }

            /* 气泡内容通用样式 */
            .bubble { 
                max-width: 85%; 
                padding: 12px 16px; 
                border-radius: 12px; 
                font-size: 0.95em; 
                position: relative; 
                word-wrap: break-word;
            }

            /* AI 气泡样式 (深灰色背景) */
            .bubble.ai { 
                background-color: #252526; 
                color: #d4d4d4; 
                border-bottom-left-radius: 2px; /* 小尾巴效果 */
            }

            /* 用户气泡样式 (蓝色背景) */
            .bubble.user { 
                background-color: #007acc; 
                color: #ffffff; 
                border-bottom-right-radius: 2px; /* 小尾巴效果 */
            }

            /* 头像/图标 */
            .avatar { 
                font-size: 1.2em; 
                margin: 0 8px; 
                align-self: flex-start; 
            }

            /* 代码块样式 */
            .code-block { 
                background-color: #1e1e1e; 
                border: 1px solid #444; 
                border-radius: 6px; 
                padding: 10px; 
                margin: 10px 0; 
                overflow-x: auto; 
                font-family: 'Consolas', 'Courier New', monospace; 
                font-size: 0.9em; 
            }
            
            .code-header { 
                color: #9cdcfe; 
                font-size: 0.8em; 
                margin-bottom: 5px; 
                display: block; 
                text-transform: uppercase; 
                letter-spacing: 1px; 
                font-weight: bold;
            }
        </style>
        """;

    /**
     * 将消息转换为 HTML 片段
     */
    public static String render(String text, boolean isUser) {
        StringBuilder html = new StringBuilder();

        // 1. 外层容器：根据 isUser 决定 flex 方向 (左或右)
        String rowClass = isUser ? "message-row user" : "message-row ai";
        String bubbleClass = isUser ? "bubble user" : "bubble ai";

        html.append("<div class='").append(rowClass).append("'>");
        // 2. 如果是 AI，头像在左边；如果是用户，头像在右边（通过 CSS 顺序或 HTML 结构调整）
        // 这里为了简单，我们将头像放在 HTML 结构里，通过 CSS 控制位置，或者直接写在对应位置
        // 3. 气泡内容
        html.append("<div class='").append(bubbleClass).append("'>");
        // 4. 解析文本中的代码块
        parseContent(html, text);
        html.append("</div>"); // 结束 bubble
        html.append("</div>"); // 结束 row
        return html.toString();
    }

    // 解析文本和代码块
    private static void parseContent(StringBuilder html, String text) {
        Pattern pattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // 普通文本
            String prefix = text.substring(lastEnd, matcher.start());
            html.append(escapeHtml(prefix)).append("<br>");

            // 代码块
            String language = matcher.group(1) != null ? matcher.group(1) : "text";
            String code = matcher.group(2);

            html.append("<div class='code-block'>");
            html.append("<span class='code-header'>").append(language.toUpperCase()).append("</span>");
            html.append("<pre>").append(escapeHtml(code)).append("</pre>");
            html.append("</div>");

            lastEnd = matcher.end();
        }

        // 剩余文本
        if (lastEnd < text.length()) {
            html.append(escapeHtml(text.substring(lastEnd)));
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }
}