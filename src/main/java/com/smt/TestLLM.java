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

    public static List<ChatMessage> chatMessageList = new ArrayList<>();

    public static void main(String[] args) {
       LLMManager llmManager = new LLMManager("C:\\Users\\smt\\IdeaProjects\\TestProject");
       chatMessageList.add(UserMessage.from("帮我写一个冒泡排序算法"));
       logger.info(llmManager.classification(chatMessageList).content());


    }

}
