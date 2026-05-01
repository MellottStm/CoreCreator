package com.smt.LangChain;

import com.smt.Cache.Configure;
import com.smt.LangChain.Bean.ResultBean;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LLMManager {

    private static final String TAG = "ToolsExecute";

    private static final Logger logger = Logger.getLogger(TAG);

    private String dirPath;

    private List<ChatMessage> chatMessageList = new ArrayList<>();

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

    public OpenAiStreamingChatModel createStreamModel () {
        if (Configure.LLM_NAME != null && Configure.LLM_URL != null && Configure.API_KEY != null) {
            return OpenAiStreamingChatModel.builder()
                    .apiKey(Configure.API_KEY)
                    .modelName(Configure.LLM_NAME)
                    .baseUrl(Configure.LLM_URL)
                    .build();
        }
        return null;
    }

    private ToolsAssistant intentAssistant;

    public LLMManager (String dirPath) {
        if (createModel() == null || createStreamModel() == null) {
            return;
        }
        this.dirPath = dirPath;
        intentAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(new ToolsManager.intentTool())
                .streamingChatModel(createStreamModel())
                .build();
    }



    public Result<ToolsPrompt.intentClass> classification (List<ChatMessage> chatMessageList) {
        return intentAssistant.intentClassification(chatMessageList);
    }


    public CompletableFuture<List<ResultBean>> requestLLMStream (List<ChatMessage> chatMessageList,RequestCallBack callBack) {
        CompletableFuture<List<ResultBean>> completableFuture = new CompletableFuture<>();
        completableFuture.join();
        return completableFuture;
    }





    public interface RequestCallBack {

        void streamResult (String result);

        void finalResult(String result);

    }




}
