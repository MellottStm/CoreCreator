package com.smt.LangChain;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ToolsPrompt {

    public static String TAG = "ToolsPrompt";

    public final static Logger logger = Logger.getLogger(TAG);

    public static String getFilePathAndContentPrompt(String dir) {
        // 👉 输入目录路径
        Path startPath = Paths.get(dir);
        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            logger.info("路径不存在或不是目录！");
            return null;
        }
        StringBuffer result = new StringBuffer();
        result.append("读取用户的项目目录所有的文件内容如下:\n");
        try {
            Stream<Path> paths = Files.walk(startPath);
            paths.filter(Files::isRegularFile) // 只处理文件
            .forEach(path -> {
                try {
                    // 读取文件内容（默认 UTF-8）
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    result.append("文件路径为:").append(path.toAbsolutePath()).append("的文件内容为:\n").append(content).append("\n");
                } catch (IOException e) {
                    logger.info("读取失败: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.warn("读取文件失败:" + e);
        }
        return result.toString();
    }


}
