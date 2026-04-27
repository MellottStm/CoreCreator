package com.smt;

import com.alibaba.fastjson.JSONObject;
import com.smt.LangChain.LLMManager;
import com.smt.LangChain.ToolsPrompt;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static void main(String[] args) {
//        LLMManager llmManager = new LLMManager("F:\\ATest\\ATest");
//        List<ChatMessage> chatMessageList = new ArrayList<>();
//        chatMessageList.add(UserMessage.from("帮我写一个查询例程，新建一个用户类（单独一个文件），用户类有一个id属性，我希望输入id，输出对应id的用户!"));
//        JSONObject resJson = JSONObject.parseObject(llmManager.chat(chatMessageList));
//        for (int i = 0;i < resJson.getJSONArray("result").size();i++) {
//            JSONObject json =  resJson.getJSONArray("result").getJSONObject(i);
//            logger.info("输出的内容:" + json.getString("content"));
//            logger.info("输出的路径:" + json.getString("path"));
//            logger.info("更改的类型:" + json.getString("type"));
//        }
        logger.info(ToolsPrompt.LLMCodePrompt);
        logger.info(ToolsPrompt.intentClassificationPrompt);

    }

}
