package com.smt.Editor;

import com.smt.Controller.DiffController;

public class DiffFile {

    public String originalText;

    public  String modifiedText;

    public int[] leftHighlightLines;

    public int[] rightHighlightLines;

    public DiffController.HighType leftHighlightType;

    public  DiffController.HighType rightHighlightType;

    public DiffFile(String original, String modified, int[] leftLines, DiffController.HighType leftType, int[] rightLines, DiffController.HighType rightType) {
        this.originalText = original;
        this.modifiedText = modified;
        this.leftHighlightLines = leftLines;
        this.rightHighlightLines = rightLines;
        this.leftHighlightType = leftType;
        this.rightHighlightType = rightType;
    }

}
