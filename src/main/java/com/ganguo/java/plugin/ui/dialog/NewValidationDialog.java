package com.ganguo.java.plugin.ui.dialog;

import com.ganguo.java.plugin.ui.form.NewValidationForm;
import com.ganguo.java.plugin.ui.utils.InputLimit;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NewValidationDialog extends BaseDialog<NewValidationForm, NewValidationDialog.Action> {

    private final AnActionEvent event;
    private InputLimit nameLimit;
    private InputLimit typeLimit;

    public NewValidationDialog(AnActionEvent e, Action action) {
        super("New Validation", new NewValidationForm(), action);
        event = e;
        initComponent();
    }

    private void initComponent() {
        nameLimit = new InputLimit(mForm.getNameField(), "^([a-zA-Z][a-zA-Z\\d]*)?$");
        typeLimit = new InputLimit(mForm.getTypeField(), "^([a-zA-Z][a-zA-Z\\d]*)?$");
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mForm.getNameField();
    }

    @Override
    protected void doOKAction() {
        String path = mForm.getPathField().getText().trim();
        String name = StringUtils.capitalize(mForm.getNameField().getText().trim());
        String type = StringUtils.capitalize(mForm.getTypeField().getText().trim());

        if (!name.isEmpty() && !type.isEmpty() &&
                nameLimit.getMatcher().test(name) && typeLimit.getMatcher().test(type) &&
                mAction.apply(event, path, name, type)) {
            super.doOKAction();
        }
    }

    public interface Action extends DialogAction {

        /**
         * 执行OK动作
         *
         * @param event AnActionEvent
         * @param path  路径
         * @param name  名称
         * @param type  类型
         */
        boolean apply(AnActionEvent event, String path, String name, String type);
    }
}
