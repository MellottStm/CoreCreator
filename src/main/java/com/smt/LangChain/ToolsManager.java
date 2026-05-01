package com.smt.LangChain;

import com.smt.LangChain.Bean.ToolFileBean;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;

public class ToolsManager {

     static class intentTool{
        @Tool(value = ToolsPrompt.intentClassificationPrompt,returnBehavior = ReturnBehavior.IMMEDIATE)
        public ToolsPrompt.intentClass getIntent(ToolsPrompt.intentClass intent){
             return intent;
        }

    }

    static class fileManageTool{

        @Tool(value = ToolsPrompt.fileManagePrompt)
        public ToolFileBean getFileType(ToolFileBean toolFileBeans){
            return toolFileBeans;
        }

    }







}
