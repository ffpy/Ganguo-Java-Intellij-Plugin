package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.context.ControllerContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.FileUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;

import java.util.Optional;

/**
 * 从接口方法跳转到测试类
 */
@Slf4j
@ImportFrom(ControllerContext.class)
public class GotoApiTestClassAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return ActionShowHelper.of(e)
                .isControllerApiMethod()
                .and(() -> Optional.ofNullable(ContextBuilder.of(this)
                        .put("event", e)
                        .build()
                        .get("apiTestFileExists", Boolean.class))
                        .orElse(false))
                .isShow();
    }

    @Func
    private void doAction(boolean apiTestFileExists, Project project, VirtualFile apiTestFile) {
        FileUtils.navigateFile(project, apiTestFile);
    }
}
