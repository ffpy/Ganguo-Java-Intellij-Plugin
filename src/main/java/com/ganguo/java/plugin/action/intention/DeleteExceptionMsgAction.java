package com.ganguo.java.plugin.action.intention;

import com.ganguo.java.plugin.constant.Filenames;
import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.IndexUtils;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.ganguo.java.plugin.util.SafeProperties;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
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
import java.util.Arrays;

/**
 * 删除ExceptionMsg
 */
@Slf4j
public class DeleteExceptionMsgAction extends BaseIntentionAction {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return "删除Msg";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!file.getVirtualFile().getPath()
                .matches(".*/src/main/resources/i18n/exception_msg.*\\.properties")) return false;

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
            WriteActions writeActions = new WriteActions(project);
            deleteOnProperties(project, key, writeActions);
            deleteOnClass(project, key, writeActions);
            writeActions.run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 从所有的exception_msg.properties文件中删除
     */
    private void deleteOnProperties(Project project, String key, WriteActions writeActions) {
        Arrays.stream(ProjectUtils.getI18nDirFile(project).getChildren())
                .filter(file -> file.getName().matches("exception_msg.*\\.properties"))
                .forEach(file -> {
                    SafeProperties properties = new SafeProperties();
                    try {
                        properties.load(file.getInputStream());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }

                    properties.remove(key);
                    writeActions.add(() -> {
                        try {
                            FileUtils.setContent(file, properties);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    });
                });
    }

    /**
     * 从ExceptionMsg.java文件中删除
     */
    private void deleteOnClass(Project project, String key, WriteActions writeActions) {
        PsiFile[] psiFiles = IndexUtils.getFilesByName(project, Filenames.MSG_CLASS);
        if (psiFiles.length == 0) {
            log.error("find {} fail!", Filenames.MSG_CLASS);
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

        writeActions.add(() -> {
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
