package com.ganguo.java.plugin.util;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;

import java.util.regex.Pattern;

public class EditorUtils {

    private static Pattern GET_EDITOR_PATTERN;

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

    /**
     * 在当前打开的编辑框中查找指定顶部类的Editor
     *
     * @param className 顶部类类名
     * @return Editor，找不到则为null
     */
    public static Editor getEditorByClassName(String className) {
        if (GET_EDITOR_PATTERN == null) {
            GET_EDITOR_PATTERN = Pattern.compile("^public\\s+class\\s+" + className + "\\s+", Pattern.MULTILINE);
        }

        Editor[] editors = EditorFactory.getInstance().getAllEditors();
        for (int i = editors.length - 1; i >= 0; i--) {
            Editor editor = editors[i];
            if (GET_EDITOR_PATTERN.matcher(editor.getDocument().getText()).find()) {
                return editor;
            }
        }
        return null;
    }
}
