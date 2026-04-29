package com.smt.Editor;

import com.smt.Controller.DiffController;

import java.util.List;

public class DiffFile {

    public String originalText;

    public  String modifiedText;

    public List<DiffController.HighType> leftHighlightTypes;

    public List<DiffController.HighType> rightHighlightTypes;

    public DiffFile(String original, String modified, List<DiffController.HighType> leftLines, List<DiffController.HighType> rightLines) {
        this.originalText = original;
        this.modifiedText = modified;
        this.leftHighlightTypes = leftLines;
        this.rightHighlightTypes = rightLines;
    }

}
