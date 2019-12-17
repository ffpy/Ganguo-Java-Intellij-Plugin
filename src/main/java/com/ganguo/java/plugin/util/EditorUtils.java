package com.ganguo.java.plugin.util;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;

import java.util.Optional;
import java.util.regex.Pattern;

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
     * 获取指定行的文本
     *
     * @param doc  文档
     * @param line 行号(0到doc.getLineCount()-1)
     * @return 文本
     */
    public static String getLineText(Document doc, int line) {
        if (line < 0 || line >= doc.getLineCount()) {
            return null;
        }
        int start = doc.getLineStartOffset(line);
        int end = doc.getLineEndOffset(line);
        return doc.getText(new TextRange(start, end));
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
    public static Optional<Editor> getEditorByClassName(String className) {
        Pattern pattern = Pattern.compile("^public\\s+class\\s+" + className, Pattern.MULTILINE);
        Editor[] editors = EditorFactory.getInstance().getAllEditors();

        for (int i = editors.length - 1; i >= 0; i--) {
            Editor editor = editors[i];
            if (pattern.matcher(editor.getDocument().getText()).find()) {
                return Optional.of(editor);
            }
        }
        return Optional.empty();
    }

    /**
     * 光标移动到指定位置
     *
     * @param editor 编辑器
     * @param offset 位置
     */
    public static void moveToOffset(Editor editor, int offset) {
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }

    /**
     * 光标移动到类的指定位置
     *
     * @param psiClass 类
     * @param offset   位置
     */
    public static void moveToClassOffset(PsiClass psiClass, int offset, WriteActions writeActions) {
        writeActions.add(() -> FileUtils.navigateFileInEditor(
                psiClass.getProject(), psiClass.getContainingFile().getVirtualFile()))
                .add(() -> getEditorByClassName(psiClass.getName()).ifPresent(editor -> moveToOffset(editor, offset)))
                .run();
    }
}
