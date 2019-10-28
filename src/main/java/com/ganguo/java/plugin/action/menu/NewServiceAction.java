package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.constant.Paths;
import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.context.NewContext;
import com.ganguo.java.plugin.ui.dialog.ModuleAndNameDialog;
import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.IndexUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 创建Service接口及实现类
 */
@Slf4j
@ImportFrom({NewContext.class, JavaFileContext.class})
public class NewServiceAction extends BaseAnAction {

    @Override
    public void action(@NotNull AnActionEvent e) {
        // 如果当前打开的文件包名路径包含admin，则新建Service默认路径为admin，否则为api
        String path = Optional.ofNullable(e.getData(LangDataKeys.HOST_EDITOR))
                .map(Editor::getDocument)
                .filter(doc -> doc.getLineCount() > 0)
                .map(doc -> EditorUtils.getLineText(doc, 0))
                .map(firstLineText -> firstLineText.contains(".admin.") ? "admin" : null)
                .orElse("api");

        new ModuleAndNameDialog(e, "New Service", true, path, this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String path, String module, String name) {
        return ContextBuilder.of(this)
                .put("event", event)
                .put("path", path)
                .put("module", path + "/" + module)
                .put("name", name)
                .build()
                .execVoid("writeFile")
                .isPresent();
    }

    /**
     * 写入对应文件
     */
    @Func
    private void writeFile(Project project, PsiDirectory moduleDir, PsiFile interFile, PsiFile implFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            FileUtils.addIfAbsent(moduleDir, interFile);
            FileUtils.addIfAbsent(moduleDir, implFile);

            FileUtils.navigateFile(project, moduleDir, interFile.getName());
        });
    }

    /**
     * Service所在的文件夹
     */
    @Var
    private VirtualFile serviceApiFile(VirtualFile packageFile) {
        return packageFile.findFileByRelativePath(Paths.SERVICE);
    }

    /**
     * Service对应的IRepository类名
     */
    @Var
    private String repositoryClassName(Project project, String name) {
        return Arrays.stream(
                IndexUtils.getFilesByName(project, "I" + name + "Repository.java"))
                .findFirst()
                .map(f -> (PsiJavaFile) f)
                .flatMap(f -> Arrays.stream(f.getClasses())
                        .findFirst()
                        .map(PsiClass::getQualifiedName))
                .orElse(null);
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String path, String name, @Nla String repositoryClassName) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", StringUtils.uncapitalize(name));
        params.put("Name", ("admin".equals(path) ? "Admin" : "") + StringUtils.capitalize(name));
        params.put("repositoryClassName", repositoryClassName);
        return params;
    }

    /**
     * Service接口文件
     */
    @Var
    private PsiFile interFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.SERVICE, "{Name}Service");
    }

    /**
     * Service实现类文件
     */
    @Var
    private PsiFile implFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.SERVICE_IMPL, "{Name}ServiceImpl");
    }

    /**
     * 所在模块文件夹
     */
    @Var
    private PsiDirectory moduleDir(FuncAction<PsiDirectory> createModuleDir) {
        return createModuleDir.get(Paths.SERVICE);
    }
}
