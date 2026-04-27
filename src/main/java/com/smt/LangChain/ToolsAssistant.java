package com.smt.LangChain;

import com.smt.LangChain.Bean.ResultBean;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

import java.util.List;

import static com.smt.LangChain.ToolsPrompt.*;

public interface ToolsAssistant {

    @SystemMessage(value = intentClassificationPrompt)
    ToolsPrompt.intentClass intentClassification (List<ChatMessage> messages);

    @SystemMessage(value = toolAgentPrompt)
    String requestLLMForWork (List<ChatMessage> messages);

    @SystemMessage(value = chatPrompt)
    String chat(List<ChatMessage> messages);

    @SystemMessage(value = toolAgentPrompt)
    TokenStream requestLLMForWorkStream (List<ChatMessage> messages);

    @SystemMessage(value = chatPrompt)
    TokenStream chatStream (List<ChatMessage> messages);


}
