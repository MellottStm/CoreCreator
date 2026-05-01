package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

@Description("基于用户提供的信息和用户请求输出需要更改的文件和更改类型AI助手的输出的数据结构")
public class ToolFileBean {

    @Description("需要更改文件的完整文件路径")
    public String path;


    @Description("更改文件的更改类型")
    public ContentBean.OperationType operationType;

}
