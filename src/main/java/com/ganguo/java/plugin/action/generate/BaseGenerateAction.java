package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGenerateAction extends BaseAnAction {

    protected abstract boolean isShow(AnActionEvent e);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isShow(e));
    }
}
