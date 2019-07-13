package com.ganguo.plugin.ui.dialog;

import com.ganguo.plugin.ui.form.AddMsgForm;
import com.ganguo.plugin.ui.utils.InputLimit;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AddMsgDialog extends BaseDialog<AddMsgForm, AddMsgDialog.Action> {

    private InputLimit mKeyLimit;

    public AddMsgDialog(Action action) {
        super("Add Msg", new AddMsgForm(), action);
    }

    @Override
    protected void initComponent() {
        mKeyLimit = new InputLimit(mForm.getKeyField(), "^[\\w ]*$");
        setOkOnEnter(mForm.getValueField());
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
        String key = mForm.getKeyField().getText().trim();
        String value = mForm.getValueField().getText().trim();

        key = key.toUpperCase().replace(' ', '_');

        if (!key.isEmpty() && !value.isEmpty() && mKeyLimit.getMatcher().test(key) &&
                mAction.apply(key, value)) {
            super.doOKAction();
        }
    }

    public interface Action extends DialogAction {

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
