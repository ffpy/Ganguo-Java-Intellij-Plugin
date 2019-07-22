package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.Filenames;
import com.ganguo.plugin.constant.Paths;
import com.ganguo.plugin.ui.dialog.AddMsgDialog;
import com.ganguo.plugin.util.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * 添加Msg
 */
@Slf4j
public class AddMsgAction extends BaseAction {

    @Override
    public void action(AnActionEvent e) {
        new AddMsgDialog(e, this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String key, String value) {
        Context context = ContextBuilder.of(this)
                .put("event", event)
                .put("key", key)
                .put("value", value)
                .build();

        Status status = context.exec("add2Properties", Status.class).get();
        if (status == Status.FAIL) return false;
        if (status == Status.EXISTS) return true;

        status = context.exec("add2Class", Status.class).get();
        if (status == Status.FAIL) return false;

        context.execVoid("paste");
        return true;
    }

    /**
     * exception_msg.properties文件
     */
    @Var
    private VirtualFile msgFile(VirtualFile rootFile) {
        return rootFile.findFileByRelativePath(Paths.MSG_PROPERTIES);
    }

    /**
     * msg的properties对象
     */
    @Var
    private SafeProperties properties(VirtualFile msgFile) {
        return ApplicationManager.getApplication().runReadAction((Computable<SafeProperties>) () -> {
            SafeProperties properties = new SafeProperties();
            try {
                properties.load(msgFile.getInputStream());
            } catch (IOException e) {
                log.error("read {} fail", Paths.MSG_PROPERTIES, e);
            }
            return properties;
        });
    }

    /**
     * 添加到exception_msg.properties中
     */
    @Func
    private Status add2Properties(VirtualFile msgFile, SafeProperties properties, String key, String value) {
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
        try {
            FileUtils.setContent(msgFile, properties);
            return Status.SUCCESS;
        } catch (IOException e) {
            log.error("write {} fail", Paths.MSG_PROPERTIES, e);
        }

        return Status.FAIL;
    }

    /**
     * ExceptionMsg.java的Class文件对象
     *
     * @param project
     * @return
     */
    @Var
    private PsiClass msgClass(Project project) {
        return Arrays.stream(FilenameIndexUtils.getFilesByName(project, Filenames.MSG_CLASS))
                .findFirst()
                .map(file -> PsiTreeUtil.findChildOfType(file, PsiClass.class))
                .orElse(null);
    }

    /**
     * 添加到到ExceptionMsg.java中
     */
    @Func
    private Status add2Class(Project project, PsiClass msgClass, PsiElementFactory elementFactory,
                             String key, String value) {
        // Key已存在
        PsiField psiField = msgClass.findFieldByName(key, false);
        if (psiField != null) {

            PsiDocComment psiDocComment = PsiTreeUtil.findChildOfType(psiField, PsiDocComment.class);
            if (psiDocComment != null) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    psiDocComment.replace(PsiUtils.createPsiDocComment(elementFactory, value));
                });
                PsiUtils.reformatJavaFile(msgClass);
            }

            return Status.EXISTS;
        }

        PsiEnumConstant psiEnumConstant = elementFactory.createEnumConstantFromText(key, null);

        PsiElement whiteSpace = PsiParserFacade.SERVICE.getInstance(project)
                .createWhiteSpaceFromText("\n\n");

        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiEnumConstant.addBefore(PsiUtils.createPsiDocComment(elementFactory, value),
                    psiEnumConstant.getFirstChild());
            psiEnumConstant.addBefore(whiteSpace, psiEnumConstant.getFirstChild());
            msgClass.add(psiEnumConstant);
        });

        PsiUtils.reformatJavaFile(msgClass);

        return Status.SUCCESS;
    }

    /**
     * 把Key和Value放到粘贴板
     */
    @Func
    private void paste(String key, String value) {
        CopyPasteUtils.putString(value);
        CopyPasteUtils.putString(key);
    }

    private enum Status {
        SUCCESS, FAIL, EXISTS
    }
}
