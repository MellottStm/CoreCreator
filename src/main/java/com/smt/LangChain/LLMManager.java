package com.smt.LangChain;

import com.smt.Cache.Configure;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecution;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class LLMManager {

    private static final String TAG = "ToolsExecute";

    private static final Logger logger = Logger.getLogger(TAG);

    private String dirPath;
    // 单例模式或静态工厂方法
    public OpenAiChatModel createModel() {
        if (Configure.LLM_NAME != null && Configure.LLM_URL != null && Configure.API_KEY != null) {
            return OpenAiChatModel.builder()
                    .apiKey(Configure.API_KEY)
                    .modelName(Configure.LLM_NAME)
                    .baseUrl(Configure.LLM_URL)
                    .build();
        }
        return null;
    }

    private ToolsAssistant assistant;

    public LLMManager (String dirPath) {
        this.dirPath = dirPath;
        LLMTools llmTools = new LLMTools();
        assistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(llmTools)
                .build();
    }

    public void requestLLM (String query) {

    }







}
