package com.ganguo.plugin.action;

import com.ganguo.plugin.ui.AddMsgForm;
import com.ganguo.plugin.ui.MyDialogWrapper;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddMsgAction extends BaseAction {

    private JFrame mFrame;

    @Override
    public void action(AnActionEvent e) {
//        new MyDialogWrapper(e, e.getProject()).show();

        mFrame = new JFrame();

        mFrame.setContentPane(new AddMsgForm(e, () -> {
            mFrame.dispose();
            mFrame = null;
        }).getMainPanel());
        mFrame.pack();

        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.setResizable(false);
        mFrame.setLocationRelativeTo(null);
        mFrame.setAlwaysOnTop(true);
        mFrame.setVisible(true);
    }
}
