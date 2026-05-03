package com.smt.LangChain;

import com.smt.Cache.Configure;
import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.Bean.FluxBean;
import com.smt.LangChain.Bean.ToolFileBean;
import com.smt.Thread.ThreadManager;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import org.apache.log4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LLMManager {

    private static final String TAG = "ToolsExecute";

    private static final Logger logger = Logger.getLogger(TAG);

    private String dirPath;

    private Disposable disposable;

    private List<ContentBean> contentList = new ArrayList<>();

    private boolean isDispose = false;

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

    private ToolsAssistant summeryAssistant;

    private TokenStream tokenStream;

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


    public Flux<Object> createLLMStream (List<ChatMessage> chatMessageList,String query) {
        return Flux.create(sink->{
            requestLLMStream(chatMessageList, query, new RequestCallBack() {
                @Override
                public void streamResult(String result) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.content = result;
                    sink.next(fluxBean);
                }

                @Override
                public void finalResult(String result) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.content = result;
                    fluxBean.isEnd = true;
                    sink.next(fluxBean);
                }

                @Override
                public void showDiff(List<ContentBean> list) {
                    FluxBean fluxBean = new FluxBean();
                    fluxBean.list = list;
                    sink.next(fluxBean);
                    sink.complete();
                }
            });
        });
    }


    public void asyncLangChain (List<ChatMessage> chatMessageList,String query, FluxCallBack callBack) {
        isDispose = false;
        contentList = new ArrayList<>();
        disposable = null;
        Flux<Void> processedFlux = createLLMStream(chatMessageList,query).publishOn(Schedulers.fromExecutor(ThreadManager.executor))
              .concatMap(bean->{
                  FluxBean fluxBean = (FluxBean) bean;
                  if (fluxBean.list != null) {
                      contentList = fluxBean.list;
                  }
                  if (fluxBean.isEnd) {
                      return Mono.fromCompletionStage(() -> callBack.finalResult(fluxBean.content));
                  } else {
                      return Mono.fromCompletionStage(() -> callBack.llmStream(fluxBean.content));
                  }
              })
              .doOnComplete(()->{
                  callBack.showDiff(contentList);
              });
        disposable = processedFlux.subscribe();
    }




    private void requestLLMStream (List<ChatMessage> chatMessageList,String query, RequestCallBack callBack) {
        StringBuffer content = new StringBuffer();
        ToolsPrompt.intentClass intentClass = classification(chatMessageList,query).content();
        if (intentClass == ToolsPrompt.intentClass.work) {
            logger.info("这是work意图!");
            callBack.streamResult("检测到用户的意图为任务意图!");
            List<ChatMessage> chatMessages = new ArrayList<>();
            chatMessages.add(SystemMessage.from("用户提供的信息:" + ToolsPrompt.getFilePathAndContentPrompt(dirPath)));
            chatMessages.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
            chatMessages.add(SystemMessage.from("用户的当前请求:" + query));
            List<ToolFileBean> beans = fileManageAssistant.fileManage(chatMessages).list;
            StringBuffer paths = new StringBuffer();
            paths.append("当前意图需要更改的文件:\n");
            for (ToolFileBean bean:beans) {
                callBack.streamResult("用户意图需要更改的文件:" + bean.path);
                paths.append(bean.path).append(",文件更改的类型：").append(bean.operationType).append("\n");
            }
            logger.info(paths);
            if (!beans.isEmpty()) {
                callBack.streamResult("正在输出内容...");
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
                tokenStream = summeryAssistant.summeryStream(summeryList)
                        .onPartialResponseWithContext((response,context)->{
                            content.append(response.text());
                            callBack.streamResult(content.toString());
                            if (isDispose) {
                                context.streamingHandle().cancel();
                            }
                        }).onError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                    callBack.finalResult(content.toString());
                            }
                        }).onCompleteResponse(new Consumer<ChatResponse>() {
                            @Override
                            public void accept(ChatResponse chatResponse) {
                                callBack.finalResult(content.toString());
                                callBack.showDiff(list);

                            }
                        });
                tokenStream.start();
            }
        } else {
            logger.info("这是chat意图!");
            List<ChatMessage> chatList = new ArrayList<>();
            chatList.add(SystemMessage.from("历史信息:" + chatMessageList.toString()));
            chatList.add(SystemMessage.from("用户的当前请求:" + query));
            tokenStream = chatAssistant.chatStream(chatList)
            .onPartialResponseWithContext((response,context)->{
                content.append(response.text());
                callBack.streamResult(content.toString());
                if (isDispose) {
                    context.streamingHandle().cancel();
                }
            }).onError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    callBack.finalResult(content.toString());

                }
            }).onCompleteResponse(new Consumer<ChatResponse>() {
                @Override
                public void accept(ChatResponse chatResponse) {
                    callBack.finalResult(content.toString());
                    callBack.showDiff(null);
                }
            });
            tokenStream.start();
        }
    }

    public void closeLLMStream() {
        if (disposable != null) {
            disposable.dispose();
        }
        logger.info("已中断Flux流!");
        isDispose = true;
    }





    public interface RequestCallBack {

        void streamResult (String result);

        void finalResult(String result);

        void showDiff (List<ContentBean> list);

    }


    public interface FluxCallBack {

        CompletableFuture<Void> llmStream(String result);

        CompletableFuture<Void> finalResult(String result);

        CompletableFuture<Void> showDiff (List<ContentBean> list);

    }




}
