package com.smt.Editor;

import eu.mihosoft.monacofx.MonacoFX;

public class MonacoAdapter implements EditorAdapter {

    private final MonacoFX editor;

    public MonacoAdapter(MonacoFX editor) {
        this.editor = editor;
    }

    @Override
    public String getText() {
        return editor.getEditor().getDocument().getText();
    }

    @Override
    public void setText(String text) {
        editor.getEditor().getDocument().setText(text);
    }
}