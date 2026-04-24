package com.smt.Editor; // 注意：根据你之前的代码，包名可能是 com.smt.Editor

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRenderer {

    // 1. 定义 JavaScript 函数
    // 这个函数会被注入到 HTML 中，用于被 Java 代码调用
    private static final String JS_SCRIPT = """
        <script>
            function updateLastAiMessage(htmlContent) {
                // 找到所有 AI 的气泡
                var bubbles = document.querySelectorAll('.message-row.ai .bubble');
                if (bubbles.length > 0) {
                    // 获取最后一个（也就是当前正在生成的那个）
                    var lastBubble = bubbles[bubbles.length - 1];
                    // 更新内容
                    lastBubble.innerHTML = htmlContent;
                }
            }
        </script>
        """;

    // 2. 将 JS 脚本合并到 CSS 样式中
    // 这样 wrapHtml 方法生成的页面就同时包含样式和脚本了
    public static final String HEAD_CONTENT = """
        <style>
            body { font-family: 'Segoe UI', sans-serif; color: #d4d4d4; background-color: #1e1e1e; margin: 0; padding: 15px; line-height: 1.6; }
            .message-row { display: flex; margin-bottom: 15px; width: 100%; }
            .message-row.ai { justify-content: flex-start; }
            .message-row.user { justify-content: flex-end; }
            .bubble { max-width: 85%; padding: 12px 16px; border-radius: 12px; font-size: 0.95em; position: relative; word-wrap: break-word; }
            .bubble.ai { background-color: #252526; color: #d4d4d4; border-bottom-left-radius: 2px; }
            .bubble.user { background-color: #007acc; color: #ffffff; border-bottom-right-radius: 2px; }
            .avatar { font-size: 1.2em; margin: 0 8px; align-self: flex-start; }
            .code-block { background-color: #1e1e1e; border: 1px solid #444; border-radius: 6px; padding: 10px; margin: 10px 0; overflow-x: auto; font-family: 'Consolas', 'Courier New', monospace; font-size: 0.9em; }
            .code-header { color: #9cdcfe; font-size: 0.8em; margin-bottom: 5px; display: block; text-transform: uppercase; letter-spacing: 1px; font-weight: bold; }
        </style>
        """ + JS_SCRIPT; // 在这里把脚本拼进去

    /**
     * 生成完整的 HTML 页面结构
     */
    public static String wrapHtml(String bodyContent) {
        // 使用 HEAD_CONTENT 替代原来的 CSS_STYLE
        return "<html><head>" + HEAD_CONTENT + "</head><body>" + bodyContent + "</body></html>";
    }

    /**
     * 渲染单条消息（用于用户消息或初始化 AI 空消息）
     */
    public static String render(String text, boolean isUser) {
        StringBuilder html = new StringBuilder();
        String rowClass = isUser ? "message-row user" : "message-row ai";
        String bubbleClass = isUser ? "bubble user" : "bubble ai";

        html.append("<div class='").append(rowClass).append("'>");

        html.append("<div class='").append(bubbleClass).append("'>");
        parseContent(html, text);
        html.append("</div>");

        html.append("</div>");
        return html.toString();
    }

    /**
     * 生成用于 JavaScript 更新的代码片段
     */
    public static String generateUpdateScript(String text) {
        StringBuilder htmlContent = new StringBuilder();
        parseContent(htmlContent, text);

        // 转义单引号和换行符，防止破坏 JS 语法
        String escapedHtml = htmlContent.toString()
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");

        // 返回调用函数的 JS 语句
        return "updateLastAiMessage('" + escapedHtml + "');";
    }

    // 解析文本和代码块（保持不变）
    private static void parseContent(StringBuilder html, String text) {
        if (text == null || text.isEmpty()) return;

        Pattern pattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            String prefix = text.substring(lastEnd, matcher.start());
            html.append(escapeHtml(prefix)).append("<br>");

            String language = matcher.group(1) != null ? matcher.group(1) : "text";
            String code = matcher.group(2);
            html.append("<div class='code-block'>");
            html.append("<span class='code-header'>").append(language.toUpperCase()).append("</span>");
            html.append("<pre>").append(escapeHtml(code)).append("</pre>");
            html.append("</div>");
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            html.append(escapeHtml(text.substring(lastEnd)));
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}