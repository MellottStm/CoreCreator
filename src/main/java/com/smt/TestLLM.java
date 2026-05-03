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


    public static void main(String[] args) {
       LLMManager llmManager = new LLMManager("C:\\小说");
       logger.info(ToolsPrompt.chatPrompt);
       String query = "你爸爸是谁？";

       llmManager.asyncLangChain(chatMessageList, query, new LLMManager.FluxCallBack() {
           @Override
           public CompletableFuture<Void> llmStream(String result) {
               return CompletableFuture.runAsync(new Runnable() {
                   @Override
                   public void run() {
                       logger.info("流式输出:" + result);
                   }
               });
           }

           @Override
           public CompletableFuture<Void> finalResult(String result) {
               return null;
           }

           @Override
           public CompletableFuture<Void> showDiff(List<ContentBean> list) {
               return CompletableFuture.runAsync(new Runnable() {
                   @Override
                   public void run() {
                       for (ContentBean bean:list) {
                           logger.info("文件:" + bean.path + ",内容:" + bean.content+ ",类型:" + bean.operationType);
                       }
                   }
               });
           }
       });



    }

}
