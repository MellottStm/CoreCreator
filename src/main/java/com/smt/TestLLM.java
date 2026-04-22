package com.smt;

import com.smt.LangChain.LLMManager;

import com.smt.LangChain.ToolsPrompt;
import org.apache.log4j.Logger;


import java.io.File;

public class TestLLM {

    public static String TAG = "TestLLM";

    public final static Logger logger = Logger.getLogger(TAG);

    public static void main(String[] args) {
        logger.info(ToolsPrompt.getFilePathAndContentPrompt("F:\\ATest\\ATest\\src\\main\\java"));
    }

}
