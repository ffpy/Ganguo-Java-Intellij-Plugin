package com.ganguo.plugin.ui.dialog;

import com.ganguo.plugin.ui.InputLimit;
import com.ganguo.plugin.ui.InputSameAs;
import com.ganguo.plugin.ui.NewRepositoryForm;
import com.ganguo.plugin.util.MyStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NewRepositoryDialog extends BaseDialog<NewRepositoryForm, NewRepositoryDialog.Action> {

    private InputLimit mModuleLimit;
    private InputLimit mNameLimit;
    private InputLimit mTabletLimit;

    public NewRepositoryDialog(String title, Action action) {
        super(title, new NewRepositoryForm(), action);
    }

    @Override
    protected void initComponent() {
        new InputSameAs(mForm.getNameField(), mForm.getModuleField(),
                text -> MyStringUtils.hump2Underline(text).toLowerCase());
        new InputSameAs(mForm.getNameField(), mForm.getTableField(),
                text -> MyStringUtils.hump2Underline(text).toUpperCase());
        mTabletLimit = new InputLimit(mForm.getTableField(), "^([A-Z_][A-Z0-9_]*)?$");
        mModuleLimit = new InputLimit(mForm.getModuleField(), "^([a-zA-Z][\\w.]*)?$");
        mNameLimit = new InputLimit(mForm.getNameField(), "^([a-zA-Z][a-zA-Z\\d]*)?$");
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mForm.getNameField();
    }

    @Override
    protected void doOKAction() {
        String table = mForm.getTableField().getText().trim();
        String module = mForm.getModuleField().getText().trim();
        String name = StringUtils.capitalize(mForm.getNameField().getText().trim());

        if (!table.isEmpty() && !name.isEmpty() &&
                mTabletLimit.getMatcher().test(table) &&
                mModuleLimit.getMatcher().test(module) &&
                mNameLimit.getMatcher().test(name) &&
                mAction.apply(table, module, name)) {
            super.doOKAction();
        }
    }

    public interface Action extends DialogAction {

        /**
         * 执行OK动作
         *
         * @param table  表名
         * @param module 模块名
         * @param name   名称
         */
        boolean apply(String table, String module, String name);
    }
}
