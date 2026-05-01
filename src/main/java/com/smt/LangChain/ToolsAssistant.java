package com.smt.LangChain;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

import static com.smt.LangChain.ToolsPrompt.*;

public interface ToolsAssistant {

    Result<intentClass> intentClassification (List<ChatMessage> messages);


    @SystemMessage(value = chatPrompt)
    TokenStream chatStream (List<ChatMessage> messages);


}
