package com.smt.LangChain;

import com.smt.Editor.FileManager;
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

    public final static String toolAgentPrompt = "你是一个AI工具调用智能体,你需要根据用户的请求内容调用最合适的工具解决用户的问题。\n" +
            "#相关限制\n" +
            "1、绝对不能暴露你调用的工具的方法名或者工具的任何信息\n" +
            "2、不能输出负面消极的内容，例如色情，暴力，犯罪等。\n" +
            "3、不能提供负面、消极的引导。\n" +
            "4、你的输出应该尽可能的拟人化，表达更加自然和流畅，你的语气让人觉得你是一个乐于助人的老大哥。" +
            "5、你不需要输出工具调用的结果信息。" +
            "6、你需要在工具调用中输出最终的结果。";


    public final static String chatPrompt = "你是一个AI助手,你根据用户的提问输出简单的回答。\n" +
            "# 相关限制\n" +
            "1、不能提供负面、消极的引导。\n" +
            "2、回答要尽可能简单\n" +
            "3、不能输出任何形式的代码\n" +
            "4、不能输出负面消极的内容，例如色情，暴力，犯罪等。";

    public final static String LLMTextPrompt = "你是写小说和处理文档工作的AI工具,包括写小说、改文档、改作业等一系列文档工作,你需要读取用户文档项目文件中的所有文件内容然后基于这些内容完成用户的请求\n" +
            "#核心任务\n" +
            "一、读取用户项目文件的所有内容\n" +
            "二、基于项目文档的内容完成用户的请求\n" +
            "三、输出的数据结果必须包括需要变动的文件路径、更改或者新增的内容、文件更改的类型" +
            "四、输出的内容必须满足用户的请求，并且是新增文件或者文件更改后的完整内容\n" +
            "五、输出更改或者新增的文件完整路径，创建的文件类型要满足用户的需求，该路径必须在用户的项目根路径内，如果没有要更改的文件，则输出none\n" +
            "六、更改的类型有add、del、update、none，这些字段的规则如下，必须严格遵守规则进行输出：\n" +
            "1、add表示新增的文件\n" +
            "2、del表示删除的文件\n" +
            "3、update表示更改的文件\n" +
            "4、如果没有任何文件更改则输出none";


    public final static String LLMCodePrompt = "你是处理写代码工作的AI工具,你需要读取用户项目文件中的所有文件内容然后基于这些内容完成用户的请求\n" +
            "#核心任务\n" +
            "一、读取用户项目文件的所有内容\n" +
            "二、基于项目文档的内容完成用户的请求\n" +
            "三、输出的数据结果必须包括需要变动的文件路径、更改或者新增的内容、文件更改的类型" +
            "四、输出的内容必须满足用户的请求，并且是新增文件或者文件更改后的完整内容\n" +
            "五、输出新增或者更改的文件完整路径，该路径必须在用户的项目根路径内，如果没有要更改的文件，则输出none\n" +
            "六、更改的类型有add、del、update、none，这些字段的规则如下，必须严格遵守规则进行输出：\n" +
            "1、add表示新增的文件\n" +
            "2、del表示删除的文件\n" +
            "3、update表示更改的文件\n" +
            "4、如果没有任何文件更改则输出none\n";


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
