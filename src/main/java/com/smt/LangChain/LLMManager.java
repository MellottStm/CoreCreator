package com.smt.LangChain;

import com.smt.Cache.Configure;
import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.Bean.ToolFileBean;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
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

    private ToolsAssistant intentAssistant;

    private ToolsAssistant chatAssistant;

    private ToolsAssistant fileManageAssistant;

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
        chatAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .streamingChatModel(createStreamModel())
                .build();
        fileManageAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(new ToolsManager.fileManageTool())
                .streamingChatModel(createStreamModel())
                .build();
    }



    public Result<ToolsPrompt.intentClass> classification (List<ChatMessage> chatMessageList) {
        return intentAssistant.intentClassification(chatMessageList);
    }




    public CompletableFuture<List<ContentBean>> requestLLMStream (List<ChatMessage> chatMessageList, RequestCallBack callBack) {
        CompletableFuture<List<ContentBean>> completableFuture = new CompletableFuture<>();
        StringBuffer content = new StringBuffer();
        ToolsPrompt.intentClass intentClass = classification(chatMessageList).content();
        if (intentClass == ToolsPrompt.intentClass.work) {
            logger.info("这是work意图!");
            this.chatMessageList.add(SystemMessage.from("用户提供的信息:" + ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
            this.chatMessageList.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
            this.chatMessageList.add(SystemMessage.from("用户的当前请求:" + ((UserMessage) chatMessageList.get(chatMessageList.size()-1)).singleText()));
            List<ToolFileBean> beans = fileManageAssistant.fileManage(this.chatMessageList).list;
            for (ToolFileBean bean:beans) {
                logger.info("当前意图需要更改的文件:" + bean.path + ",文件更改的类型：" + bean.operationType);
            }

        } else {
            logger.info("这是chat意图!");
            chatAssistant.chatStream(chatMessageList).onPartialResponse(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    content.append(s);
                    callBack.streamResult(content.toString());
                }
            }).onError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    callBack.finalResult(content.toString());
                    completableFuture.complete(null);
                }
            }).onCompleteResponse(new Consumer<ChatResponse>() {
                @Override
                public void accept(ChatResponse chatResponse) {
                    callBack.finalResult(content.toString());
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
