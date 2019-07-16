package com.ganguo.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.sun.istack.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class BaseReplaceAction extends BaseAction {

    /**
     * @param e AnActionEvent
     * @param text 选择的文本
     * @return 改变后的文本，可以为null表示不改变
     * @throws Exception 异常
     */
    @Nullable
    protected abstract String replace(AnActionEvent e, String text) throws Exception;

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) return;

        String selectedText = selectionModel.getSelectedText();
        if (StringUtils.isEmpty(selectedText)) return;
        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();

        String newText = replace(e, selectedText);
        if (newText == null) return;

        WriteCommandAction.runWriteCommandAction(project,
                () -> editor.getDocument().replaceString(start, end, newText));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean hasProject = e.getProject() != null;

        boolean hasSelectedText = Optional.ofNullable(e.getData(LangDataKeys.EDITOR))
                .map(Editor::getSelectionModel)
                .map(SelectionModel::getSelectedText)
                .map(StringUtils::isNotEmpty)
                .orElse(false);

        e.getPresentation().setEnabled(hasProject && hasSelectedText);
    }
}
