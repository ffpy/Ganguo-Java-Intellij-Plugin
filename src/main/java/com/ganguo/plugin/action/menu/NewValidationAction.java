package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.constant.Paths;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.ui.dialog.ModuleAndNameDialog;
import com.ganguo.plugin.util.FileUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.CodeContextBuilder;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建校验注解及校验类
 */
@Slf4j
public class NewValidationAction extends NewAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        new ModuleAndNameDialog(e, "New Validation", false, this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String module, String name) {
        return CodeContextBuilder.of(this)
                .put("event", event)
                .put("module", module)
                .put("name", name)
                .build()
                .execVoid("writeFile")
                .isPresent();
    }

    /**
     * 写入对应文件
     */
    @Func
    private void writeFile(Project project, PsiDirectory moduleDir,
                           PsiFile validationFile, PsiFile validationImplFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            FileUtils.addIfAbsent(moduleDir, validationFile);
            FileUtils.addIfAbsent(moduleDir, validationImplFile);

            FileUtils.navigateFile(project, moduleDir, validationFile.getName());
            FileUtils.navigateFile(project, moduleDir, validationImplFile.getName());
        });
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, String> params(String name, String packageName) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("packageName", packageName);
        return params;
    }

    /**
     * 校验注解文件
     */
    @Var
    private PsiFile validationFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.VALIDATION, "{name}").get();
    }

    /**
     * 注解实现文件
     */
    @Var
    private PsiFile validationImplFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.VALIDATION_IMPL, "{name}ValidatorImpl").get();
    }

    /**
     * 所在模块的文件夹
     */
    @Var
    private PsiDirectory moduleDir(Context context) {
        return context.exec("createModuleDir", PsiDirectory.class, Paths.VALIDATION).get();
    }
}
