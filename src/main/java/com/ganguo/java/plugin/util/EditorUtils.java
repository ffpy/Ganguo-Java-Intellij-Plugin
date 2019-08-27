package com.ganguo.java.plugin.util;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
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

    /**
     * 插入内容在光标所在行的下一行
     *
     * @param editor  Editor
     * @param content 要插入的文本
     */
    public static void insertToNextLine(Editor editor, String content) {
        LogicalPosition position = editor.getCaretModel().getLogicalPosition();
        int lineEndOffset = editor.getDocument().getLineEndOffset(position.line);
        new WriteActions(editor.getProject())
                .add(() -> editor.getDocument().insertString(lineEndOffset, content))
                .run();
    }
}
