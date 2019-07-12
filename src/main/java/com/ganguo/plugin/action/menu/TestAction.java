package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TestAction extends BaseAction {

    @Override
    public void action(@NotNull AnActionEvent e) {
        Project project = e.getProject();

    }

}
