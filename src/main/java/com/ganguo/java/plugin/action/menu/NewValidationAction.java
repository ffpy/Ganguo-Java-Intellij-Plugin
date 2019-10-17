package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.constant.Paths;
import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.context.NewContext;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.ui.dialog.ModuleAndNameDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Var;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建校验注解及校验类
 */
@Slf4j
@ImportFrom({NewContext.class, JavaFileContext.class})
public class NewValidationAction extends BaseAnAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        new ModuleAndNameDialog(e, "New Validation", false, this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String path, String module, String name) {
        return ContextBuilder.of(this)
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

            FileUtils.navigateFile(project, moduleDir, validationImplFile.getName());
            FileUtils.navigateFile(project, moduleDir, validationFile.getName());
        });
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String name, String packageName) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", StringUtils.uncapitalize(name));
        params.put("Name", StringUtils.capitalize(name));
        params.put("packageName", packageName);
        return params;
    }

    /**
     * 校验注解文件
     */
    @Var
    private PsiFile validationFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.VALIDATION, "{Name}");
    }

    /**
     * 注解实现文件
     */
    @Var
    private PsiFile validationImplFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.VALIDATION_IMPL, "{Name}ValidatorImpl");
    }

    /**
     * 所在模块的文件夹
     */
    @Var
    private PsiDirectory moduleDir(FuncAction<PsiDirectory> createModuleDir) {
        return createModuleDir.get(Paths.VALIDATION);
    }
}
