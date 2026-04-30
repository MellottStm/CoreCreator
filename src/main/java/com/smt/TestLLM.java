package com.smt;

import com.smt.LangChain.Bean.ResultBean;
import com.smt.LangChain.LLMManager;
import com.smt.LangChain.ToolsPrompt;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.internal.chat.Tool;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static void main(String[] args) {
       logger.info(ToolsPrompt.getFilePathAndContentPrompt("F:\\小说"));
//        List<ChatMessage> chatMessageList = new ArrayList<>();
//        chatMessageList.add(UserMessage.from("你爸爸是谁？"));
//        llmManager.requestLLMStream(chatMessageList, new LLMManager.RequestCallBack() {
//            @Override
//            public void streamResult(String result) {
//                logger.info("大模型的流式内容:" + result);
//            }
//
//            @Override
//            public void finalResult(String result) {
//                logger.info("大模型最终的回答:" + result);
//            }
//        }).whenComplete((resultBeanList, throwable) -> {
//            if (resultBeanList != null) {
//                for (ResultBean resultBean :resultBeanList) {
//                    logger.info( "文件修改的内容:" + resultBean.content);
//                    logger.info("文件修改的路径:" + resultBean.path);
//                    logger.info("文件更新类型:" + resultBean.operationType);
//                }
//            }
//        });
    }

}
