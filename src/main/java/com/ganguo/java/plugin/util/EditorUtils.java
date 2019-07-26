package com.ganguo.java.plugin.util;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

public class EditorUtils {

    /**
     * 获取光标所在行的文本
     *
     * @param editor Editor
     * @return 文本
     */
    public static String getCurLineText(Editor editor) {
        CaretModel caretModel = editor.getCaretModel();
        int line = caretModel.getLogicalPosition().line;
        int start = line == 0 ? 0 : editor.getDocument().getLineEndOffset(line - 1) + 1;
        int end = editor.getDocument().getLineEndOffset(line);
        return editor.getDocument().getText(new TextRange(start, end));
    }
}
