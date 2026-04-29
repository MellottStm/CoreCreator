package com.smt.Editor;

import com.smt.Controller.DiffController;

import java.util.List;

public class DiffFile {

    public String originalText;

    public  String modifiedText;

    public List<DiffController.HighType> leftHighlightTypes;

    public List<DiffController.HighType> rightHighlightTypes;

    public DiffController.HighType leftHighlightType;

    public  DiffController.HighType rightHighlightType;

    public DiffFile(String original, String modified, List<DiffController.HighType> leftLines, DiffController.HighType leftType, List<DiffController.HighType> rightLines, DiffController.HighType rightType) {
        this.originalText = original;
        this.modifiedText = modified;
        this.leftHighlightTypes = leftLines;
        this.rightHighlightTypes = rightLines;
        this.leftHighlightType = leftType;
        this.rightHighlightType = rightType;
    }

}
