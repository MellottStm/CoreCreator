package com.smt.LangChain;

import com.smt.LangChain.Bean.LLMMCPResultBean;
import com.smt.LangChain.Bean.ToolFileBean;
import com.smt.LangChain.Bean.ToolFileResultBean;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

import static com.smt.LangChain.ToolsPrompt.*;

public interface ToolsAssistant {

    Result<intentClass> intentClassification (List<ChatMessage> messages);

    ToolFileResultBean fileManage(List<ChatMessage> messages);

    LLMMCPResultBean fileContent(List<ChatMessage> messages);

    @SystemMessage(value = summeryAssistantPrompt)
    TokenStream summeryStream (List<ChatMessage> messages);

    @SystemMessage(value = chatPrompt)
    TokenStream chatStream (List<ChatMessage> messages);


}
