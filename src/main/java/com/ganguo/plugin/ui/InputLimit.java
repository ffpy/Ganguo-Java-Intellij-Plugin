package com.ganguo.plugin.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class InputLimit implements DocumentListener {

    private final JTextField mTextField;
    private final Predicate<String> mMatcher;
    private String mOldText;

    public InputLimit(JTextField textField, String pattern) {
        this.mTextField = textField;
        this.mMatcher = Pattern.compile(pattern).asPredicate();
        this.mOldText = textField.getText();

        mTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        String text = mTextField.getText();

        if (mMatcher.test(text)) {
            mOldText = text;
        } else {
            EventQueue.invokeLater(() -> mTextField.setText(mOldText));
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        mOldText = mTextField.getText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    public Predicate<String> getMatcher() {
        return mMatcher;
    }
}
