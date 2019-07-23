package com.ganguo.plugin.ui.dialog;

import com.ganguo.plugin.ui.form.ModuleAndNameForm;
import com.ganguo.plugin.ui.utils.InputLimit;
import com.ganguo.plugin.ui.utils.InputSameAs;
import com.ganguo.plugin.util.MyStringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ModuleAndNameDialog extends BaseDialog<ModuleAndNameForm, ModuleAndNameDialog.Action> {

    private InputLimit mModuleLimit;
    private InputLimit mNameLimit;
    private AnActionEvent mEvent;
    private boolean mModuleSameAsName;

    public ModuleAndNameDialog(AnActionEvent e, String title, Action action) {
        this(e, title, true, action);
    }

    public ModuleAndNameDialog(AnActionEvent e, String title, boolean moduleSameAsName, Action action) {
        super(title, new ModuleAndNameForm(), action);
        mEvent = e;
        mModuleSameAsName = moduleSameAsName;
        initComponent();
    }

    private void initComponent() {
        if (mModuleSameAsName) {
            new InputSameAs(mForm.getNameField(), mForm.getModuleField(),
                    text -> MyStringUtils.camelCase2UnderScoreCase(text).toLowerCase());
        }
        mModuleLimit = new InputLimit(mForm.getModuleField(), "^([a-zA-Z][\\w.]*)?$");
        mNameLimit = new InputLimit(mForm.getNameField(), "^([a-zA-Z][a-zA-Z\\d]*)?$");
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mModuleSameAsName ? mForm.getNameField() : mForm.getModuleField();
    }

    @Override
    protected void doOKAction() {
        String module = mForm.getModuleField().getText().trim();
        String name = StringUtils.capitalize(mForm.getNameField().getText().trim());

        if (!name.isEmpty() &&
                mModuleLimit.getMatcher().test(module) && mNameLimit.getMatcher().test(name) &&
                mAction.apply(mEvent, module, name)) {
            super.doOKAction();
        }
    }

    public interface Action extends DialogAction {

        /**
         * 执行OK动作
         *
         * @param event  AnActionEvent
         * @param module 模块名
         * @param name   名称
         */
        boolean apply(AnActionEvent event, String module, String name);
    }
}
