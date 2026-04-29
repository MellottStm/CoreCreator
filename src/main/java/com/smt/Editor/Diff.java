package com.smt.Editor;

public class Diff {

    public enum DiffTag {
        CHANGE,
        DEL,
        INSERT,
        EQUAL
    }

    public DiffTag tag;

    public String originalValue;

    public String modifiedValue;

    public Diff () {

    }

    public Diff (DiffTag tag,String originalValue,String modifiedValue) {
        this.tag = tag;
        this.originalValue = originalValue;
        this.modifiedValue = modifiedValue;
    }


}
