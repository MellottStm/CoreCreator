package com.smt;

import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.Bean.LLMMCPResultBean;
import com.smt.LangChain.LLMManager;
import com.smt.LangChain.ToolsPrompt;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static List<ChatMessage> chatMessageList = new ArrayList<>();

    public static CompletableFuture<List<ContentBean>> llmmcpResultBeanCompletableFuture;

    public static void main(String[] args) {
       LLMManager llmManager = new LLMManager("C:\\小说");
       logger.info(ToolsPrompt.chatPrompt);
       String query = "帮我续写第二章,要新建一个文件";
       llmmcpResultBeanCompletableFuture = new CompletableFuture<>();
       llmmcpResultBeanCompletableFuture = llmManager.requestLLMStream(chatMessageList, query,new LLMManager.RequestCallBack() {
           @Override
           public void streamResult(String result) {
               logger.info("流式输出:" + result);
               llmmcpResultBeanCompletableFuture.complete(null);
           }

           @Override
           public void finalResult(String result) {
               logger.info("完整的回答:" + result);
               chatMessageList.add(UserMessage.from(query));
               chatMessageList.add(AiMessage.from(result));
           }
       });
       llmmcpResultBeanCompletableFuture.whenComplete((result,e) -> {
               logger.info("已完成！");

            for (ContentBean bean:result) {
                logger.info("文件:" + bean.path + ",内容:" + bean.content+ ",类型:" + bean.operationType);
            }
        });



    }

}
