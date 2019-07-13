package com.ganguo.plugin.ui;

import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.Objects;
import java.util.function.Function;

public class InputSameAs {

    private final JTextField mFromField;
    private final JTextField mToField;
    private final Function<String, String> mMap;
    private boolean isSameAs = true;

    public InputSameAs(JTextField fromField, JTextField toField) {
        this(fromField, toField, null);
    }

    public InputSameAs(JTextField fromField, JTextField toField, @Nullable Function<String, String> map) {
        mFromField = Objects.requireNonNull(fromField);
        mToField = Objects.requireNonNull(toField);
        mMap = map == null ? Function.identity() : map;
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
                    mToField.setText(mMap.apply(mFromField.getText()));
                }
            }
        });
    }
}
