package com.ganguo.plugin.action.intention;

import com.ganguo.plugin.action.menu.AddMsgAction;
import com.ganguo.plugin.util.EditorUtils;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.FilenameIndexUtils;
import com.ganguo.plugin.util.SafeProperties;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 删除Msg
 */
@Slf4j
public class DeleteMsgAction implements IntentionAction {

    private static final String FAMILY_NAME = "Ganguo";
    private static final String FILENAME_MSG = "exception_msg.properties";

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return "删除Msg";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!FILENAME_MSG.equals(file.getName())) return false;

        String lineText = EditorUtils.getCurLineText(editor).trim();

        if (lineText.startsWith("#")) return false;
        return lineText.contains("=");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        String lineText = EditorUtils.getCurLineText(editor);
        if (StringUtils.isEmpty(lineText)) return;

        String[] strs = StringUtils.split(lineText, '=');
        // 格式不正确，忽略
        if (strs == null || strs.length != 2) return;

        String key = strs[0];

        try {
            deleteOnProperties(file.getVirtualFile(), key);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        deleteOnClass(project, key);
    }

    private void deleteOnProperties(VirtualFile file, String key) throws IOException {
        SafeProperties properties = new SafeProperties();
        properties.load(file.getInputStream());
        properties.remove(key);
        FileUtils.setContent(file, properties);
    }

    private void deleteOnClass(Project project, String key) {
        PsiFile[] psiFiles = FilenameIndexUtils.getFilesByName(project, AddMsgAction.FILENAME_MSG_CLASS);
        if (psiFiles.length == 0) {
            log.error("find {} fail!", AddMsgAction.FILENAME_MSG_CLASS);
            return;
        }
        PsiFile psiFile = psiFiles[0];

        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
        if (psiClass == null) {
            log.error("find class fail!");
            return;
        }

        PsiField psiField = psiClass.findFieldByName(key, false);
        if (psiField == null) return;

        PsiElement psiWhiteSpace = psiField.getPrevSibling();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (psiWhiteSpace instanceof PsiWhiteSpace) {
                psiWhiteSpace.delete();
            }
            psiField.delete();
        });
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
