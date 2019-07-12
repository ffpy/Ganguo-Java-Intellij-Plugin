package com.ganguo.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AddMsgDialogWrapper extends DialogWrapper implements DocumentListener {

    private final AddMsgForm mForm;
    private final Action mAction;
    private final Predicate<String> mKeyMatch = Pattern.compile("^[\\w\\d_ ]*$").asPredicate();
    private String mOldKeyText = "";

    public AddMsgDialogWrapper(Action action) {
        super(true);
        this.mAction = Objects.requireNonNull(action);
        mForm = new AddMsgForm();

        init();
        setTitle("Add Msg");

        mForm.getKeyField().getDocument().addDocumentListener(this);

        mForm.getValueField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doOKAction();
                }
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mForm.getMainPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mForm.getKeyField();
    }

    @Override
    protected void doOKAction() {
        if (mAction.apply(mForm.getKeyField().getText(), mForm.getValueField().getText())) {
            super.doOKAction();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        String text = mForm.getKeyField().getText();
        if (!mKeyMatch.test(text)) {
            EventQueue.invokeLater(() -> mForm.getKeyField().setText(mOldKeyText));
        } else {
            mOldKeyText = text;
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        mOldKeyText = mForm.getKeyField().getText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }


    public interface Action {

        /**
         * 执行Ok动作
         *
         * @param key   键
         * @param value 值
         * @return true关闭对话框，false不关闭对话框
         */
        boolean apply(String key, String value);
    }
}
