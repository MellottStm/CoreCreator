package com.smt.LangChain;


import dev.langchain4j.agent.tool.Tool;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class LLMTools {

    private static String TAG = "LLMTools";

    public final static Logger logger = Logger.getLogger(TAG);

    private CompletableFuture<String> toolTypeCompletableFuture;
    @Tool(value=ToolsPrompt.intentClassificationPrompt)
    public void intentClassification (String intent) {
        logger.info("工具分类的结果:" + intent);
    }




}
