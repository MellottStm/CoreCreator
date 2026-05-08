package com.smt;

import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.LLMManager;
import com.smt.LangChain.ToolsPrompt;
import dev.langchain4j.data.message.ChatMessage;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static List<ChatMessage> chatMessageList = new ArrayList<>();


    public static void main(String[] args) {
       LLMManager llmManager = new LLMManager("F:\\TestProject\\Test");
       String query = "帮我写一个okhttp的get请求，并且添加依赖到相关文件中";
       long token = System.currentTimeMillis();
       llmManager.asyncLangChain(chatMessageList, query,token ,new LLMManager.FluxCallBack() {
           @Override
           public CompletableFuture<Void> llmStream(String result,long token) {
               return CompletableFuture.runAsync(new Runnable() {
                   @Override
                   public void run() {
                       logger.info("中间输出:" + result);
                   }
               });
           }

           @Override
           public CompletableFuture<Void> finalResult(String result,long token) {
               return CompletableFuture.runAsync(new Runnable() {
                   @Override
                   public void run() {

                   }
               });
           }

           @Override
           public void showDiff(List<ContentBean> list, long token) {
               for (ContentBean bean : list) {
                   logger.info("文件:" + bean.path + ",内容:" + bean.content + ",类型:" + bean.operationType);
               }
           }
       });



    }

}
