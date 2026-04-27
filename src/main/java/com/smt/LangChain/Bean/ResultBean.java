package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

@Description("AI助手的工作意图输出格式")
public class ResultBean {

    public enum OperationType {
        add,
        del,
        update,
        none
    }

    @Description("输出的文件内容")
    public StringBuffer content;


    @Description("更改的文件路径")
    public String path;

    @Description("文件的更改类型")
    public OperationType operationType;


}
