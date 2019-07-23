package com.ganguo.plugin.ui.dialog;

import com.ganguo.plugin.ui.BaseForm;
import com.ganguo.plugin.ui.utils.TypeEnterListener;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public abstract class BaseDialog<T extends BaseForm, A extends DialogAction> extends DialogWrapper {

    protected final T mForm;
    protected final A mAction;

    public BaseDialog(String title, T form, A action) {
        super(true);

        this.mForm = Objects.requireNonNull(form);
        this.mAction = Objects.requireNonNull(action);

        init();
        setTitle(Objects.requireNonNull(title));

        setOKButtonText("确定");
        setCancelButtonText("取消");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mForm.getMainPanel();
    }

    /**
     * 设置当按下回车时执行Ok动作
     *
     * @param component JComponent
     */
    protected void setOkOnEnter(JComponent component) {
        component.addKeyListener(new TypeEnterListener(this::doOKAction));
    }
}
