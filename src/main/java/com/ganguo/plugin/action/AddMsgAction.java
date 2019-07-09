package com.ganguo.plugin.action;

import com.ganguo.plugin.ui.AddMsgForm;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;

import javax.swing.*;

public class AddMsgAction extends AnAction implements DumbAware {

    private JFrame mFrame;

    @Override
    public void actionPerformed(AnActionEvent e) {
        mFrame = new JFrame();

        mFrame.getContentPane().add(new AddMsgForm(e, () -> {
            mFrame.dispose();
            mFrame = null;
        }).getMainPanel());
        mFrame.pack();
        mFrame.setLocationRelativeTo(null);
        mFrame.setAlwaysOnTop(true);
        mFrame.setVisible(true);
    }
}
