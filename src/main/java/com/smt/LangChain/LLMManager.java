package com.smt.LangChain;

import com.smt.Cache.Configure;
import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.Bean.FluxBean;
import com.smt.LangChain.Bean.ToolFileBean;
import com.smt.Thread.ThreadManager;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import org.apache.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LLMManager {

    private static final String TAG = "ToolsExecute";

    private static final Logger logger = Logger.getLogger(TAG);

    private String dirPath;

    private List<ContentBean> contentList = new ArrayList<>();

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

    private ToolsAssistant contentManageAssistant;

    private ToolsAssistant needReadAssistant;

    private ToolsAssistant summeryAssistant;

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
        needReadAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(new ToolsManager.readFileTool())
                .streamingChatModel(createStreamModel())
                .build();
        contentManageAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .tools(new ToolsManager.contentManageTool())
                .streamingChatModel(createStreamModel())
                .build();
        summeryAssistant = AiServices.builder(ToolsAssistant.class)
                .chatModel(createModel())
                .streamingChatModel(createStreamModel())
                .build();
    }


    public Result<ToolsPrompt.intentClass> classification (List<ChatMessage> chatMessageList,String query) {
        List<ChatMessage> intentChatMessages = new ArrayList<>();
        intentChatMessages.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
        intentChatMessages.add(SystemMessage.from("用户的当前请求:" + query));
        return intentAssistant.intentClassification(intentChatMessages);
    }


    public Flux<Object> createLLMStream (List<ChatMessage> chatMessageList,String query,long token) {
        return Flux.create(sink->{
            requestLLMStream(chatMessageList, query,token,new RequestCallBack() {
                @Override
                public void streamResult(String result,long token) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.content = result;
                    sink.next(fluxBean);
                }

                @Override
                public void finalResult(String result,long token) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.content = result;
                    fluxBean.isEnd = true;
                    sink.next(fluxBean);
                }

                @Override
                public void showDiff(List<ContentBean> list,long token) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.list = list;
                    sink.next(fluxBean);
                    sink.complete();
                }
            });
        });
    }


    public void asyncLangChain (List<ChatMessage> chatMessageList,String query,long token,FluxCallBack callBack) {
        contentList = new ArrayList<>();
        Flux<Void> processedFlux = createLLMStream(chatMessageList,query,token).publishOn(Schedulers.fromExecutor(ThreadManager.executor))
              .concatMap(bean->{
                  FluxBean fluxBean = (FluxBean) bean;
                  if (fluxBean.list != null) {
                      contentList = fluxBean.list;
                  }
                  if (fluxBean.isEnd) {
                      return Mono.fromCompletionStage(() -> callBack.finalResult(fluxBean.content,token));
                  } else {
                      return Mono.fromCompletionStage(() -> callBack.llmStream(fluxBean.content,token));
                  }
              })
              .doOnComplete(()->{
                  callBack.showDiff(contentList,token);
              });
       processedFlux.subscribe();
    }




    private void requestLLMStream (List<ChatMessage> chatMessageList,String query,long token,RequestCallBack callBack) {
        StringBuffer content = new StringBuffer();
        ToolsPrompt.intentClass intentClass = classification(chatMessageList,query).content();
        if (intentClass == ToolsPrompt.intentClass.work) {
            logger.info("这是work意图!");
            callBack.streamResult("检测到用户的意图为任务意图!",token);
            List<ChatMessage> chatMessages = new ArrayList<>();
            chatMessages.add(SystemMessage.from("用户提供的信息:" + ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
            chatMessages.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
            chatMessages.add(SystemMessage.from("用户的当前请求:" + query));
            List<ToolFileBean> beans = fileManageAssistant.fileManage(chatMessages).list;
            StringBuffer paths = new StringBuffer();
            paths.append("当前意图需要更改的文件:\n");
            for (ToolFileBean bean:beans) {
                callBack.streamResult("用户意图需要更改的文件:" + bean.path,token);
                paths.append(bean.path).append(",文件更改的类型：").append(bean.operationType).append("\n");
            }
            logger.info(paths);
            if (!beans.isEmpty()) {
                callBack.streamResult("正在输出内容...",token);
                List<ChatMessage> contentChatMessages = new ArrayList<>();
                contentChatMessages.add(SystemMessage.from("用户提供的信息:" + ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
                contentChatMessages.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
                contentChatMessages.add(SystemMessage.from("用户的当前请求:" + query));
                contentChatMessages.add(SystemMessage.from(paths.toString()));
                List<ContentBean> list = contentManageAssistant.fileContent(contentChatMessages).list;
                StringBuilder finalResult = new StringBuilder();
                for (ContentBean bean:list) {
                    finalResult.append("用户更改的文件:").append(bean.path).append(",更改的内容为:").append(bean.content).append(",更改的类型是:").append(bean.operationType);
                }
                List<ChatMessage> summeryList = new ArrayList<>();
                summeryList.add(SystemMessage.from("用户的最终输出内容:" + finalResult));
                summeryList.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
                summeryList.add(SystemMessage.from("用户的当前请求:" + query));
                summeryAssistant.summeryStream(summeryList)
                    .onPartialResponseWithContext((response,context)->{
                        content.append(response.text());
                        callBack.streamResult(content.toString(),token);
                    }).onError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                                callBack.finalResult(content.toString(),token);
                        }
                    }).onCompleteResponse(new Consumer<ChatResponse>() {
                        @Override
                        public void accept(ChatResponse chatResponse) {
                            callBack.finalResult(content.toString(),token);
                            callBack.showDiff(list,token);

                        }
                    }).start();
            }
        } else {
            logger.info("这是chat意图!");
            List<ChatMessage> chatList = new ArrayList<>();
            chatList.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
            chatList.add(SystemMessage.from("用户的当前请求:" + query));
            chatAssistant.chatStream(chatList)
            .onPartialResponseWithContext((response,context)->{
                content.append(response.text());
                callBack.streamResult(content.toString(),token);
            }).onError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    callBack.finalResult(content.toString(),token);

                }
            }).onCompleteResponse(new Consumer<ChatResponse>() {
                @Override
                public void accept(ChatResponse chatResponse) {
                    callBack.finalResult(content.toString(),token);
                    callBack.showDiff(null,token);
                }
            }).start();
        }
    }





    public interface RequestCallBack {

        void streamResult (String result,long token);

        void finalResult(String result,long token);

        void showDiff (List<ContentBean> list,long token);

    }


    public interface FluxCallBack {

        CompletableFuture<Void> llmStream(String result,long currentToken);

        CompletableFuture<Void> finalResult(String result,long currentToken);

        void showDiff (List<ContentBean> list, long currentToken);

    }




}
