package com.smt.Editor;

import javafx.scene.control.TextArea;

public class TextAreaAdapter implements EditorAdapter {

    private final TextArea textArea;

    public TextAreaAdapter(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void setText(String text) {
        textArea.setText(text);
    }
}