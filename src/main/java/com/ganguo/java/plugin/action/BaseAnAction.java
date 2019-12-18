package com.ganguo.java.plugin.action;

import com.ganguo.java.plugin.context.BaseContext;
import com.ganguo.java.plugin.util.NotificationHelper;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

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

            // 打印错误信息到日志文件
            try {
                File file = new File(ProjectUtils.getRootFile(event.getProject()).getPath() +
                        "/log/GanguoJavaPlugin.log");
                FileUtils.forceMkdirParent(file);
                e.printStackTrace(new PrintStream(new FileOutputStream(file, true)));
            } catch (Exception ex) {
                NotificationHelper.error(ex.getMessage()).show();
            }

            if (e.getMessage() != null) {
                NotificationHelper.error(e.getMessage()).show();
            }
        } finally {
            this.mEvent = null;
        }
    }
}
