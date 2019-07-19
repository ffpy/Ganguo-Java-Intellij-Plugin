package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.ui.dialog.ModuleAndNameDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.FilenameIndexUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.CodeContextBuilder;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建Service接口及实现类
 */
@Slf4j
public class NewServiceAction extends NewAction {

    private static final String PATH_SERVICE_API = "service/api";

    @Override
    public void action(@NotNull AnActionEvent e) {
        new ModuleAndNameDialog(e, "New Service", this::doAction).show();
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

    @Func
    private void writeFile(Project project, PsiDirectory moduleDir, PsiFile interFile, PsiFile implFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            FileUtils.addIfAbsent(moduleDir, interFile);
            FileUtils.addIfAbsent(moduleDir, implFile);

            FileUtils.navigateFile(project, moduleDir, interFile.getName());
        });
    }

    @Var
    private VirtualFile serviceApiFile(VirtualFile packageFile) {
        return packageFile.findFileByRelativePath(PATH_SERVICE_API);
    }

    @Var
    private String repositoryClassName(Project project, String name) {
        return Arrays.stream(
                FilenameIndexUtils.getFilesByName(project, "I" + name + "Repository.java"))
                .findFirst()
                .map(f -> (PsiJavaFile) f)
                .flatMap(f -> Arrays.stream(f.getClasses())
                        .findFirst()
                        .map(PsiClass::getQualifiedName))
                .orElse(null);
    }

    @Var
    private Map<String, String> params(String name, @Nla String repositoryClassName) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("repositoryClassName", repositoryClassName);
        return params;
    }

    @Var
    private PsiFile interFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.SERVICE, "{name}Service").get();
    }

    @Var
    private PsiFile implFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.SERVICE_IMPL, "{name}ServiceImpl").get();
    }

    @Var
    private PsiDirectory moduleDir(Context context) {
        return context.exec("createModuleDir", PsiDirectory.class, PATH_SERVICE_API).get();
    }
}
