package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.context.ControllerContext;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.ui.dialog.InputDialog;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.util.Optional;

/**
 * 修改接口方法名称，并同步修改测试类
 */
@Slf4j
@ImportFrom({JavaFileContext.class, ControllerContext.class})
public class ModifyApiMethodNameAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Context context = ContextBuilder.of(this)
                .put("event", e)
                .put("newMethodName", "")
                .build();
        String oldMethodName = context.get("curMethodName", String.class);

        new InputDialog("输入新的方法名称", "方法名称", oldMethodName, "^[a-z][a-zA-Z0-9]*$", value -> {
            if (value.equals(oldMethodName)) {
                return false;
            }
            return context.update("newMethodName", value)
                    .exec("doAction", Boolean.class)
                    .orElse(false);
        }).show();
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return ActionShowHelper.of(e).isControllerApiMethod().isShow();
    }

    @Func
    private boolean doAction(VirtualFile apiTestFile, String newMethodName, PsiMethod curMethod,
                             String newTestClassName, Project project,
                             WriteActions writeActions) {
        writeActions.add(() -> {
            // 修改方法名
            curMethod.setName(newMethodName);

            PsiFile testPsiFile = PsiManager.getInstance(project).findFile(apiTestFile);
            if (testPsiFile instanceof PsiJavaFile) {
                try {
                    // 修改测试类文件名
                    Optional.ofNullable(PsiUtils.getClassByFile((PsiJavaFile) testPsiFile))
                            .ifPresent(file -> file.setName(newTestClassName));
                    // 修改测试类类名
                    apiTestFile.rename(this, newTestClassName + ".java");
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).run();
        return true;
    }

    @Var
    private String newTestClassName(String newMethodName, FuncAction<String> getTestClassNamByMethodName) {
        return getTestClassNamByMethodName.exec(newMethodName).get();
    }

    @Var
    private String curMethodName(PsiMethod curMethod) {
        return curMethod.getName();
    }
}
