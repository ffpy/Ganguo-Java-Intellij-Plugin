package com.ganguo.plugin.ui;

import com.ganguo.plugin.utils.CopyPasteUtils;
import com.ganguo.plugin.utils.MsgUtils;
import com.ganguo.plugin.utils.PsiUtils;
import com.ganguo.plugin.utils.SafeProperties;
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

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class AddMsgForm {
    private static final String PATH_MSG_PROPERTIES = "src/main/resources/i18n/exception_msg.properties";
    private static final String FILENAME_MSG_CLASS = "ExceptionMsg.java";

    private JPanel mainPanel;
    private JTextField mKeyField;
    private JTextField mValueField;
    private JButton mCancelButton;
    private JButton mOkButton;
    private AnActionEvent mEvent;
    private Runnable mOnCancel;

    public AddMsgForm(AnActionEvent event, Runnable onCancel) {
        this.mEvent = event;
        this.mOnCancel = onCancel;

        mCancelButton.addActionListener(e -> mOnCancel.run());
        mOkButton.addActionListener(e -> onConfirm());

        mKeyField.requestFocus();

        KeyAdapter confirmKeyListener = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onConfirm();
                }
            }
        };

        mValueField.addKeyListener(confirmKeyListener);
        mOkButton.addKeyListener(confirmKeyListener);
        mCancelButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    mOnCancel.run();
                }
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void onConfirm() {
        try {
            String key = mKeyField.getText();
            String value = mValueField.getText();

            if (key.isEmpty() || value.isEmpty()) {
                return;
            }

            key = key.toUpperCase().replace(' ', '_');

            Project project = mEvent.getProject();
            VirtualFile projectFile = Optional.ofNullable(project)
                    .map(Project::getProjectFile)
                    .map(VirtualFile::getParent)
                    .map(VirtualFile::getParent)
                    .orElse(null);
            if (projectFile == null) {
                MsgUtils.error("project file not found!");
                return;
            }

            if (!add2Properties(project, projectFile, key, value)) {
                return;
            }

            if (!add2Class(project, key, value)) {
                return;
            }


            mOnCancel.run();

            //把key放到粘贴板
            CopyPasteUtils.putString(key);
        } catch (Exception e) {
            MsgUtils.error(e.getMessage());
        }
    }

    /**
     * 添加到配置文件
     */
    private boolean add2Properties(Project project, VirtualFile projectFile, String key, String value) {
        // 获取配置文件
        VirtualFile msgFile = projectFile.findFileByRelativePath(PATH_MSG_PROPERTIES);
        if (msgFile == null || !msgFile.exists()) {
            MsgUtils.error("%s not found!", PATH_MSG_PROPERTIES);
            return false;
        }

        try {
            SafeProperties properties = new SafeProperties();
            properties.load(msgFile.getInputStream());

            // 检查Key是否已存在
            if (properties.containsKey(key)) {
                int result = Messages.showYesNoDialog(key + "已存在，是否覆盖？",
                        "提示", "覆盖", "取消", null);
                if (result != Messages.YES) {
                    return false;
                }
            }

            // 检查Value是否已存在
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (value.equals(entry.getValue())) {
                    CopyPasteUtils.putString(entry.getKey().toString());
                    MsgUtils.info("发现%s已存在，已放入粘贴板", entry.getKey());
                    mOnCancel.run();
                    return false;
                }
            }

            // 保存到文件
            properties.setProperty(key, value);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            properties.store(bos, null);
            msgFile.setBinaryContent(bos.toByteArray());
            msgFile.refresh(true, false);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            MsgUtils.error("read %s fail!", PATH_MSG_PROPERTIES);
        }
        return false;
    }

    /**
     * 添加到Class文件
     */
    private boolean add2Class(Project project, String key, String value) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, FILENAME_MSG_CLASS,
                GlobalSearchScope.projectScope(project));
        if (psiFiles.length == 0) {
            MsgUtils.error("find %s fail!", FILENAME_MSG_CLASS);
            return false;
        }
        PsiFile psiFile = psiFiles[0];

        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
        if (psiClass == null) {
            MsgUtils.error("find class fail!");
            return false;
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

            return true;
        }

        PsiEnumConstant psiEnumConstant = factory.createEnumConstantFromText(key, null);

        PsiElement whiteSpace = PsiParserFacade.SERVICE.getInstance(project)
                .createWhiteSpaceFromText("\n\n");

        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiEnumConstant.addBefore(PsiUtils.createPsiDocComment(factory, value),
                    psiEnumConstant.findElementAt(0));
            psiEnumConstant.addBefore(whiteSpace, psiEnumConstant.findElementAt(0));
            psiClass.add(psiEnumConstant);
        });

        PsiUtils.reformatJavaFile(psiClass);

        return true;
    }
}
