package com.smt.Editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRenderer {

    // 简单的深色主题 CSS
    public static final String CSS_STYLE = """
        <style>
            body { font-family: 'Segoe UI', sans-serif; color: #d4d4d4; background-color: #1e1e1e; margin: 0; padding: 10px; line-height: 1.6; }
            .user-msg { color: #4ec9b0; font-weight: bold; margin-top: 10px; }
            .ai-msg { color: #dcdcaa; margin-top: 10px; }
            .text-content { margin-bottom: 10px; }
            /* 代码块样式 - 模拟编辑器背景 */
            .code-block { background-color: #252526; border: 1px solid #444; border-radius: 4px; padding: 10px; margin: 10px 0; overflow-x: auto; font-family: 'Consolas', 'Courier New', monospace; font-size: 0.9em; }
            .code-header { color: #9cdcfe; font-size: 0.8em; margin-bottom: 5px; display: block; text-transform: uppercase; letter-spacing: 1px; }
        </style>
        """;

    /**
     * 将纯文本转换为 HTML，自动识别代码块
     * 支持格式: ```java ... ``` 或 ```json ... ```
     */
    public static String render(String text, boolean isUser) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head>").append(CSS_STYLE).append("</head><body>");

        // 添加角色标识
        if (isUser) {
            html.append("<div class='user-msg'>User:</div>");
        } else {
            html.append("<div class='ai-msg'>AI Assistant</div>");
        }

        html.append("<div class='text-content'>");

        // 使用正则解析代码块
        // 匹配 ```语言 内容 ```
        Pattern pattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // 1. 输出代码块前面的普通文本
            String prefix = text.substring(lastEnd, matcher.start());
            html.append(escapeHtml(prefix)).append("<br>");

            // 2. 输出代码块
            String language = matcher.group(1) != null ? matcher.group(1) : "text";
            String code = matcher.group(2);

            html.append("<div class='code-block'>");
            html.append("<span class='code-header'>").append(language.toUpperCase()).append("</span>");
            html.append("<pre>").append(escapeHtml(code)).append("</pre>");
            html.append("</div>");

            lastEnd = matcher.end();
        }

        // 3. 输出剩余的文本
        if (lastEnd < text.length()) {
            html.append(escapeHtml(text.substring(lastEnd)));
        }

        html.append("</div></body></html>");
        return html.toString();
    }

    // 简单的 HTML 转义，防止 XSS 和格式错乱
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }

}
