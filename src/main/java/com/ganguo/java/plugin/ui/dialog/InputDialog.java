package com.ganguo.java.plugin.ui.dialog;

import com.ganguo.java.plugin.ui.form.InputDialogForm;
import com.ganguo.java.plugin.ui.utils.InputLimit;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class InputDialog extends BaseDialog<InputDialogForm, InputDialog.Action> {

    private InputLimit inputLimit;

    public InputDialog(String title, String label, @Nullable String value, @Nullable String pattern, Action action) {
        super(title, new InputDialogForm(), action);
        initComponent(label, value, pattern);
    }

    private void initComponent(String label, @Nullable String value, @Nullable String pattern) {
        mForm.getKeyField().setText(Objects.requireNonNull(label));
        if (StringUtils.isNotEmpty(value)) {
            mForm.getValueField().setText(value);
        }
        if (pattern != null) {
            inputLimit = new InputLimit(mForm.getValueField(), pattern);
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mForm.getValueField();
    }

    @Override
    protected void doOKAction() {
        String value = mForm.getValueField().getText();

        if (inputLimit == null || inputLimit.getMatcher().test(value)) {
            if (mAction.ok(value)) {
                super.doOKAction();
            }
        }
    }

    public interface Action extends DialogAction {
        boolean ok(String value);
    }
}
