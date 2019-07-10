package com.ganguo.plugin.action;

import com.ganguo.plugin.util.MsgUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAction extends AnAction implements DumbAware {

    protected abstract void action(AnActionEvent e);

    @Override
    @Deprecated
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            action(event);
        } catch (Exception e) {
            MsgUtils.error(e.getMessage());
        }
    }

    protected boolean noProject(Project project) {
        if (project == null) {
            MsgUtils.error("get project fail!");
            return true;
        }
        return false;
    }
}
