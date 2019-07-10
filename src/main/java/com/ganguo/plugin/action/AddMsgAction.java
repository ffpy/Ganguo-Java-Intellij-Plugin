package com.ganguo.plugin.action;

import com.ganguo.plugin.ui.AddMsgForm;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

public class AddMsgAction extends BaseAction {

    private JFrame mFrame;

    @Override
    public void action(AnActionEvent e) {
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
