package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.ui.dialog.AddMsgDialog;
import com.ganguo.plugin.util.CopyPasteUtils;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MsgUtils;
import com.ganguo.plugin.util.PsiUtils;
import com.ganguo.plugin.util.SafeProperties;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class AddMsgAction extends BaseAction {

    public static final String PATH_MSG_PROPERTIES = "src/main/resources/i18n/exception_msg.properties";
    public static final String FILENAME_MSG_CLASS = "ExceptionMsg.java";

    private AnActionEvent mEvent;

    @Override
    public void action(AnActionEvent e) {
        mEvent = e;
        new AddMsgDialog(this::doAction).show();
    }

    private boolean doAction(String key, String value) {
        try {
            Project project = mEvent.getProject();
            VirtualFile projectFile = Optional.ofNullable(project)
                    .map(Project::getProjectFile)
                    .map(VirtualFile::getParent)
                    .map(VirtualFile::getParent)
                    .orElse(null);
            if (projectFile == null) {
                MsgUtils.error("project file not found!");
                return false;
            }

            Status status = add2Properties(projectFile, key, value);
            if (status == Status.FAIL) return false;
            if (status == Status.EXISTS) return true;

            if (add2Class(project, key, value) == Status.FAIL) return false;

            CopyPasteUtils.putString(value);
            CopyPasteUtils.putString(key);

            return true;
        } catch (Exception e) {
            MsgUtils.error(e.getMessage());
        }
        return false;
    }

    /**
     * 添加到配置文件
     */
    private Status add2Properties(VirtualFile projectFile, String key, String value) {
        // 获取配置文件
        VirtualFile msgFile = projectFile.findFileByRelativePath(PATH_MSG_PROPERTIES);
        if (msgFile == null || !msgFile.exists()) {
            MsgUtils.error("%s not found!", PATH_MSG_PROPERTIES);
            return Status.FAIL;
        }

        try {
            SafeProperties properties = new SafeProperties();
            properties.load(msgFile.getInputStream());

            // 检查Value是否已存在
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (value.equals(entry.getValue())) {
                    CopyPasteUtils.putString(entry.getValue().toString());
                    CopyPasteUtils.putString(entry.getKey().toString());

                    MsgUtils.info("发现%s已存在，已放入粘贴板", entry.getValue());

                    return Status.EXISTS;
                }
            }

            // 检查Key是否已存在
            if (properties.containsKey(key)) {
                int result = Messages.showYesNoDialog(key + "已存在，是否覆盖？",
                        "提示", "覆盖", "取消", null);
                if (result != Messages.YES) {
                    return Status.FAIL;
                }
            }

            properties.setProperty(key, value);
            FileUtils.setContent(msgFile, properties);

            return Status.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            MsgUtils.error("read %s fail!", PATH_MSG_PROPERTIES);
        }
        return Status.FAIL;
    }

    /**
     * 添加到Class文件
     */
    private Status add2Class(Project project, String key, String value) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, FILENAME_MSG_CLASS,
                GlobalSearchScope.projectScope(project));
        if (psiFiles.length == 0) {
            MsgUtils.error("find %s fail!", FILENAME_MSG_CLASS);
            return Status.FAIL;
        }
        PsiFile psiFile = psiFiles[0];

        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
        if (psiClass == null) {
            MsgUtils.error("find class fail!");
            return Status.FAIL;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);


        // Key已存在
        PsiField psiField = psiClass.findFieldByName(key, false);
        if (psiField != null) {

            PsiDocComment psiDocComment = PsiTreeUtil.findChildOfType(psiField, PsiDocComment.class);
            if (psiDocComment != null) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    psiDocComment.replace(PsiUtils.createPsiDocComment(factory, value));
                });
                PsiUtils.reformatJavaFile(psiClass);
            }

            return Status.EXISTS;
        }

        PsiEnumConstant psiEnumConstant = factory.createEnumConstantFromText(key, null);

        PsiElement whiteSpace = PsiParserFacade.SERVICE.getInstance(project)
                .createWhiteSpaceFromText("\n\n");

        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiEnumConstant.addBefore(PsiUtils.createPsiDocComment(factory, value),
                    psiEnumConstant.getFirstChild());
            psiEnumConstant.addBefore(whiteSpace, psiEnumConstant.getFirstChild());
            psiClass.add(psiEnumConstant);
        });

        PsiUtils.reformatJavaFile(psiClass);

        return Status.SUCCESS;
    }

    private enum Status {
        SUCCESS, FAIL, EXISTS
    }
}
