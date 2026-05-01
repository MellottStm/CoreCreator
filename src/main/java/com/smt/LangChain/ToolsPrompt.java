package com.smt.LangChain;

import com.smt.Editor.FileManager;
import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ToolsPrompt {

    public static String TAG = "ToolsPrompt";

    public final static Logger logger = Logger.getLogger(TAG);

    public enum intentClass {
        chat,
        work
    }


    public final static String intentClassificationPrompt = "功能描述:你是一个用户意图分类工具\n" +
            "#核心任务：\n" +
            "你主要将用户的意图分为以下两类:\n" +
            "1、用户的工作需求。包括更改、修改、代写、实现代码、代写小说或者涉及到修改用户项目文件的问题等等。\n" +
            "2、用户的闲聊需求。包括日常聊天、问好、询问知识或者一些无关紧要的蠢逼问题。\n" +
            "#输出\n" +
            "只允许输出以下结果\n" +
            "1、work,表示是用户的工作需求。\n" +
            "2、chat,表示是用户的闲聊需求。";

    public final static String chatPrompt = "你是一个AI助手,你根据用户的提问输出简单的回答。\n" +
            "# 相关限制\n" +
            "1、不能提供负面、消极的引导。\n" +
            "2、回答要尽可能简单\n" +
            "3、不能输出任何形式的代码\n" +
            "4、不能输出负面消极的内容，例如色情，暴力，犯罪等。";

    public final static String classificationChangeFilePrompt = "";


    public static String getFilePathAndContentPrompt(String dir) {
        // 👉 输入目录路径
        Path startPath = Paths.get(dir);
        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            logger.info("路径不存在或不是目录！");
            return null;
        }
        StringBuffer result = new StringBuffer();
        result.append("用户的项目根目录是:").append(dir).append("\n");
        result.append("读取用户的项目目录所有的文件内容如下:\n");
        try {
            Stream<Path> paths = Files.walk(startPath);
            paths.filter(Files::isRegularFile) // 只处理文件
            .forEach(path -> {
                // 读取文件内容（默认 UTF-8）
                String content = FileManager.readProjectFileContent(path);
                if (content != null) {
                    result.append("文件路径为:").append(path.toAbsolutePath()).append("的文件内容为:\n").append(content).append("\n");
                } else {
                    result.append("该项目有文件:").append(path.toAbsolutePath()).append("\n");
                }
            });

        } catch (Exception e) {
            logger.warn("读取文件失败:" + e);
        }
        logger.info(result.toString());
        return result.toString();
    }


}
