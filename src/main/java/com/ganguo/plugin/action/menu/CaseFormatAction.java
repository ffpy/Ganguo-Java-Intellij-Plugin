package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.util.MyStringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

/**
 * Format between CamelCase and UnderScoreCase
 */
public class CaseFormatAction extends BaseAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) return;

        Project project = e.getProject();
        if (project == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) return;

        String selectedText = selectionModel.getSelectedText();
        if (StringUtils.isEmpty(selectedText)) return;
        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();

        String newText = selectedText.contains("_") ? MyStringUtils.underScoreCase2CamelCase(selectedText) :
                MyStringUtils.camelCase2UnderScoreCase(selectedText);

        WriteCommandAction.runWriteCommandAction(project,
                () -> editor.getDocument().replaceString(start, end, newText));
    }
}
