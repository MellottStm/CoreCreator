package com.smt.LangChain.Bean;


import com.smt.LangChain.ToolsPrompt;
import dev.langchain4j.model.output.structured.Description;

@Description("基于用户提供的信息和用户的请求以及提供的文件路径和文件更改类型这些信息输出指定内容的AI助手输出的数据结构")
public class ContentBean {
    @Description("输出的内容")
    public StringBuffer content;
    @Description("输出内容对应的文件路径")
    public String path;
    @Description("输出内容对呀的文件更改类型")
    public ToolsPrompt.OperationType operationType;


}
