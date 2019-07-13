package com.ganguo.plugin.ui;

import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.Objects;

public class InputSameAs {

    private final JTextField mFromField;
    private final JTextField mToField;
    private boolean isSameAs = true;

    public InputSameAs(JTextField fromField, JTextField toField) {
        this.mFromField = Objects.requireNonNull(fromField);
        this.mToField = Objects.requireNonNull(toField);
        init();
    }

    private void init() {
        mToField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (isSameAs && mToField.hasFocus()) {
                    isSameAs = false;
                }
            }
        });
        mFromField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (isSameAs) {
                    mToField.setText(mFromField.getText());
                }
            }
        });
    }
}
