package com.smt.LangChain;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;

public class ToolsManager {

     static class intentTool{

         @Tool(value = ToolsPrompt.intentClassificationPrompt,returnBehavior = ReturnBehavior.IMMEDIATE)
        public ToolsPrompt.intentClass getIntent(ToolsPrompt.intentClass intent){
             return intent;
        }

    }

}
