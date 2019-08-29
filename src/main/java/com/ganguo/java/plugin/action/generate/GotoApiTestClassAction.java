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

@Slf4j
@ImportFrom(ControllerContext.class)
public class GotoApiTestClassAction extends BaseGenerateAction {
    private static final Context context = ContextBuilder.of(new GotoApiTestClassAction())
            .put("event", null)
            .build();

    @Override
    protected void action(AnActionEvent e) throws Exception {
        context.clearCache()
                .update("event", e)
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        context.clearCache().update("event", e);
        return ActionShowHelper.of(e)
                .isControllerApiMethod()
                .and(() -> Optional.ofNullable(context.get("apiTestFileExists", Boolean.class))
                        .orElse(false))
                .isShow();
    }

    @Func
    private void doAction(boolean apiTestFileExists, Project project, VirtualFile apiTestFile) {
        FileUtils.navigateFile(project, apiTestFile);
    }
}
