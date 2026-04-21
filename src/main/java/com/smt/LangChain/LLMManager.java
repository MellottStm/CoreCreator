package com.smt.LangChain;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.log4j.Logger;

public class LLMManager {

    private static final String TAG = "ToolsExecute";

    private static final Logger logger = Logger.getLogger(TAG);

    private OpenAiChatModel model1_5PRO = OpenAiChatModel.builder()
            .apiKey("d9fca42a-bd84-46fc-898f-4d04eada5ae6")
            .modelName("ep-20250414124020-qq6d7")
            .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
            .build();

}
