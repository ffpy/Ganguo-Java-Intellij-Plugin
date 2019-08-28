package com.ganguo.java.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;

import java.util.LinkedList;
import java.util.List;

public class WriteActions {
    private final Project project;
    private final List<Runnable> tasks = new LinkedList<>();

    public WriteActions(Project project) {
        this.project = project;
    }

    public WriteActions add(Runnable task) {
        tasks.add(task);
        return this;
    }

    public void run() {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            tasks.forEach(Runnable::run);
            tasks.clear();
        });
    }
}
