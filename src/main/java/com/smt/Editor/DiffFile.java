package com.smt.Editor;

import com.smt.Controller.DiffController;

import java.util.List;

public class DiffFile {

    public String originalText;

    public  String modifiedText;

    public List<Integer> leftHighlightLines;

    public List<Integer> rightHighlightLines;

    public DiffController.HighType leftHighlightType;

    public  DiffController.HighType rightHighlightType;

    public DiffFile(String original, String modified, List<Integer> leftLines, DiffController.HighType leftType,List<Integer> rightLines, DiffController.HighType rightType) {
        this.originalText = original;
        this.modifiedText = modified;
        this.leftHighlightLines = leftLines;
        this.rightHighlightLines = rightLines;
        this.leftHighlightType = leftType;
        this.rightHighlightType = rightType;
    }

}
