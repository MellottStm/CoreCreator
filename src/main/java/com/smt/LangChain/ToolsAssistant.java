package com.smt.LangChain;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;

public interface ToolsAssistant {

    String chat(List<ChatMessage> messages);

    String intent(String query);

}
