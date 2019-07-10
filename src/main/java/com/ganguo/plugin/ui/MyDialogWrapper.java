package com.ganguo.plugin.ui;

import com.ganguo.plugin.util.MsgUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyDialogWrapper extends DialogWrapper {

    private AnActionEvent mEvent;

    public MyDialogWrapper(AnActionEvent event, @Nullable Project project) {
        super(project, true);
        this.mEvent = event;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new AddMsgForm(mEvent, () -> {
        }).getMainPanel();
        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        MsgUtils.info("ok");
    }
}
