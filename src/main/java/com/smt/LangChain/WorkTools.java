package com.smt.LangChain;

import com.smt.LangChain.Bean.ResultBean;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkTools {

    CompletableFuture<List<ResultBean>> completableFuture;

    public void setCompletableFuture (CompletableFuture<List<ResultBean>> completableFuture) {
        this.completableFuture = completableFuture;
    }

    @Tool(value = ToolsPrompt.LLMCodePrompt)
    public void codeWorker (List<ResultBean> resultBeanList) {
        completableFuture.complete(resultBeanList);
    }


    @Tool(value = ToolsPrompt.LLMTextPrompt)
    public void textWorker (List<ResultBean> resultBeanList) {
        completableFuture.complete(resultBeanList);
    }


}
