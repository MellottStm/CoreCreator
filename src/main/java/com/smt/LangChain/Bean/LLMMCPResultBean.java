package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;


@Description("基于用户提供的信息和用户的请求以及提供的文件路径和文件更改类型这些信息输出指定内容的AI助手输出的最终结果的数据结构")
public class LLMMCPResultBean {

   @Description("基于用户提供的信息和用户的请求以及提供的文件路径和文件更改类型这些信息输出指定内容的AI助手输出的最终结果")
   public List<ContentBean> list;

}
