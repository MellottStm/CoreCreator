package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

@Description("AI助手的工作意图输出数据结构")
public class ResultBean {

    public enum OperationType {
        add,
        del,
        update,
        none
    }

    @Description("文件更改后的完整内容")
    public StringBuffer content;


    @Description("需要更改的文件路径")
    public String path;

    @Description("文件的更改类型")
    public OperationType operationType;


}
