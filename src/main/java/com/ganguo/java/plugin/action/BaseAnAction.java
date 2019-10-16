package com.ganguo.java.plugin.action;

import com.ganguo.java.plugin.context.BaseContext;
import com.ganguo.java.plugin.util.NotificationHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ForceImportFrom(BaseContext.class)
public abstract class BaseAnAction extends AnAction implements DumbAware {

    protected AnActionEvent mEvent;

    protected abstract void action(AnActionEvent e) throws Exception;

    @Override
    @Deprecated
    public void actionPerformed(@NotNull AnActionEvent event) {
        this.mEvent = event;
        try {
            action(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            NotificationHelper.error(e.getMessage()).show();
        } finally {
            this.mEvent = null;
        }
    }
}
