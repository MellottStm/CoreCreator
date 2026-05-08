package com.smt.LangChain.Bean;

import dev.langchain4j.model.output.structured.Description;

import java.util.ArrayList;
import java.util.List;

@Description("基于用户提供的信息和用户请求输出任何可能需要参考的文件完整路径的工具输出数据结构")
public class LLMNeedReadFileBean {

    @Description("基于用户提供的信息和用户请求输出任何可能需要参考的文件完整路径的工具输出最终结果")
    public List<String> needReadFileList = new ArrayList<>();

}
