package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

@Description("基于用户提供的信息和用户请求输出需要更改的文件和更改类型AI助手输出的最终内容的数据结构")
public class ToolFileResultBean {

    @Description("基于用户提供的信息和用户请求输出需要更改的文件和更改类型AI助手输出的最终内容")
    public List<ToolFileBean> list;

}
