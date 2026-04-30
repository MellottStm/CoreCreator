package com.smt.LangChain;

import com.smt.LangChain.Bean.ResultBean;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkTools {

    private final String TAG = "WorkTools";

    public final  Logger logger = Logger.getLogger(TAG);

    CompletableFuture<List<ResultBean>> completableFuture;

    public void setCompletableFuture (CompletableFuture<List<ResultBean>> completableFuture) {
        this.completableFuture = completableFuture;
    }

    @Tool(value = ToolsPrompt.LLMCodePrompt)
    public void codeWorker (@P("根据用户的请求输出的最终结果")List<ResultBean> resultBeanList,@P("用户的请求") String query) {
        logger.info("用户的query:" + query);
        completableFuture.complete(resultBeanList);
    }


    @Tool(value = ToolsPrompt.LLMTextPrompt)
    public void textWorker (@P("根据用户的请求输出的最终结果")List<ResultBean> resultBeanList,@P("用户的请求")String query) {
        logger.info("用户的query:" + query);
        completableFuture.complete(resultBeanList);
    }


}
