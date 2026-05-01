package com.smt;

import com.smt.LangChain.Bean.ContentBean;
import com.smt.LangChain.LLMManager;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static List<ChatMessage> chatMessageList = new ArrayList<>();

    public static void main(String[] args) {
       LLMManager llmManager = new LLMManager("C:\\Users\\smt\\IdeaProjects\\TestProject");
       chatMessageList.add(UserMessage.from("帮我写一个okhttp的post请求，以及帮我添加相关依赖"));
       llmManager.requestLLMStream(chatMessageList, new LLMManager.RequestCallBack() {
           @Override
           public void streamResult(String result) {
               logger.info("流式输出:" + result);
           }

           @Override
           public void finalResult(String result) {
               logger.info("完整的回答:" + result);
           }
       }).whenComplete((result,e) -> {
           for (ContentBean bean:result) {
               logger.info("文件:" + bean.path + ",内容:" + bean.content+ ",类型:" + bean.operationType);
           }
       });


    }

}
