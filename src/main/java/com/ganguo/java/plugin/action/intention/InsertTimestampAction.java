package com.ganguo.java.plugin.action.intention;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * 插入当前时间戳
 */
@Slf4j
public class InsertTimestampAction extends BaseIntentionAction {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return "插入当前时间戳";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        editor.getDocument().insertString(offset, Long.toString(Instant.now().getEpochSecond()));
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
