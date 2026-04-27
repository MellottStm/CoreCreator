package com.smt.LangChain;

import com.smt.Cache.Configure;
import com.smt.LangChain.Bean.ResultBean;
import com.sun.javafx.css.parser.Token;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    private ToolsAssistant assistant;

    private WorkTools workTools;

    public LLMManager (String dirPath) {
        this.dirPath = dirPath;
        workTools = new WorkTools();
        assistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(workTools)
                .streamingChatModel(createStreamModel())
                .build();
    }



    private ToolsPrompt.intentClass classification (List<ChatMessage> chatMessageList) {
        return assistant.intentClassification(chatMessageList);
    }

    private String chat (List<ChatMessage> chatMessageList) {
        this.chatMessageList.addAll(chatMessageList);
        return assistant.chat(this.chatMessageList);
    }

    private TokenStream chatStream (List<ChatMessage> chatMessageList) {
        this.chatMessageList.addAll(chatMessageList);
        return assistant.chatStream(this.chatMessageList);
    }



    private String requestLLMForWork(List<ChatMessage> chatMessageList) {
        this.chatMessageList.add(SystemMessage.systemMessage(ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
        this.chatMessageList.addAll(chatMessageList);
        return assistant.requestLLMForWork(this.chatMessageList);
    }

    private TokenStream requestLLMForWorkStream(List<ChatMessage> chatMessageList) {
        this.chatMessageList.add(SystemMessage.systemMessage(ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
        this.chatMessageList.addAll(chatMessageList);
        return assistant.requestLLMForWorkStream(this.chatMessageList);
    }

    public CompletableFuture<List<ResultBean>> requestLLM (List<ChatMessage> chatMessageList,RequestCallBack callBack) {
        CompletableFuture<List<ResultBean>> completableFuture = new CompletableFuture<>();
        workTools.setCompletableFuture(completableFuture);
        ToolsPrompt.intentClass intentClass = classification(chatMessageList);
        if (intentClass.equals(ToolsPrompt.intentClass.work)) {
            callBack.finalResult(requestLLMForWork(chatMessageList));
        } else {
            callBack.finalResult(chat(chatMessageList));
            completableFuture.complete(null);
        }
        completableFuture.join();
        return completableFuture;
    }





    public CompletableFuture<List<ResultBean>> requestLLMStream (List<ChatMessage> chatMessageList,RequestCallBack callBack) {
        CompletableFuture<List<ResultBean>> completableFuture = new CompletableFuture<>();
        workTools.setCompletableFuture(completableFuture);
        ToolsPrompt.intentClass intentClass = classification(chatMessageList);
        StringBuffer resultBuffer = new StringBuffer();
        if (intentClass.equals(ToolsPrompt.intentClass.work)) {
            requestLLMForWorkStream(chatMessageList).onPartialResponse(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    resultBuffer.append(s);
                    callBack.streamResult(resultBuffer.toString());
                }
            }).onError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    callBack.finalResult(resultBuffer.toString());
                    completableFuture.complete(null);
                }
            }).onCompleteResponse(new Consumer<ChatResponse>() {
                @Override
                public void accept(ChatResponse chatResponse) {
                    callBack.finalResult(resultBuffer.toString());
                }
            }).start();
        } else {
            chatStream(chatMessageList).onPartialResponse(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    resultBuffer.append(s);
                    callBack.streamResult(resultBuffer.toString());
                }
            }).onCompleteResponse(new Consumer<ChatResponse>() {
                @Override
                public void accept(ChatResponse chatResponse) {
                    callBack.finalResult(resultBuffer.toString());
                    completableFuture.complete(null);
                }
            }).onError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    callBack.finalResult(resultBuffer.toString());
                    completableFuture.complete(null);
                }
            }).start();
        }
        completableFuture.join();
        return completableFuture;
    }





    public interface RequestCallBack {

        void streamResult (String result);

        void finalResult(String result);

    }




}
