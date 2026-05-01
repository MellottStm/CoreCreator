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

    public enum OperationType {
        add,
        del,
        update,
        none
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


    public final static String fileManagePrompt = "功能描述：你是一个基于用户提供的信息和用户请求输出需要更改的文件和更改类型的AI助手。\n" +
            "#核心任务\n" +
            "1、你需要基于用户提供的信息知道用户当前路径下的所有文件和文件内容。\n" +
            "2、你需要基于用户当前路径的所有文件和文件内容分析用户的请求，输出需要更改文件的完整文件路径\n" +
            "3、你需要输出更改文件的更改类型，类型包括新增文件、删除文件、更改文件\n" +
            "#相关限制\n" +
            "1、你更改的文件必须在用户当前路径下，不能超出当前的路径\n" +
            "2、更改文件类型必须只能输出add、del、update，其中add表示新增文件，del表示删除文件，update表示更改文件\n";

    public final static String fileContentPrompt = "功能描述:你是一个基于用户提供的信息和用户的请求以及提供的文件路径和文件更改类型这些信息输出指定内容的AI助手\n" +
            "#核心任务\n" +
            "1、你需要基于用户提供的信息知道用户当前路径下的所有文件和文件内容，你需要参考这些文件内容。\n" +
            "2、你需要基于用户当前路径的所有文件和文件内容分析用户的请求\n" +
            "3、你需要基于提供的文件路径和文件更改类型进行输出，输出的规则如下：\n" +
            "1、如果文件的更改类型是del，则输出的内容是空的\n" +
            "2、如果文件的更改类型是update和add，则你需要参考用户提供的信息和用户的请求输出文件中完整的内容。\n" +
            "#相关限制\n" +
            "1、你更改文件必须在用户当前路径下，不能超出当前的路径。\n" +
            "2、输出的内容必须要和文件的路径和文件的更改类型进行匹配";


    public final static String summeryAssistantPrompt = "功能描述：你需要根据用户的最终输出内容，还有用户的历史信息和请求进行总结\n" +
            "# 相关限制\n" +
            "1、不能提供负面、消极的引导。\n" +
            "2、回答要尽可能简单\n" +
            "3、不能输出任何形式的代码\n" +
            "4、不能输出负面消极的内容，例如色情，暴力，犯罪等。\n" +
            "5、你的输出需要尽量的拟人化和口语化。";


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
