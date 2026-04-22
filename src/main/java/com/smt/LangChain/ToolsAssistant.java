package com.smt.LangChain;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface ToolsAssistant {

    TokenStream intentStream(String query);

    String intent(String query);

}
