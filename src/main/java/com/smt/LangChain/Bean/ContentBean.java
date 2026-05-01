package com.smt.LangChain.Bean;


public class ContentBean {

    public enum OperationType {
        add,
        del,
        update,
        none
    }

    public StringBuffer content;

    public String path;

    public OperationType operationType;


}
