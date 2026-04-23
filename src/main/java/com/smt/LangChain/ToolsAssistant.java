package com.smt.LangChain;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;

import java.util.List;

public interface ToolsAssistant {

    String chat(List<ChatMessage> messages);

    TokenStream requestLLM (List<ChatMessage> messages);

}
