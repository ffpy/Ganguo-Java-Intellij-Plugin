package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.util.DialogUtils;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MsgUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.StringHelper;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 位于顶部菜单栏
 */
public class NewServiceAction extends BaseAction {

    public static final String PATH_SERVICE_API = "service/api";

    @Override
    public void action(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (noProject(project)) return;
        assert project != null;

        VirtualFile packageFile = ProjectUtils.getPackageFile(project);
        if (packageFile == null) return;

        VirtualFile serviceApiFile = packageFile.findFileByRelativePath(PATH_SERVICE_API);
        if (serviceApiFile == null) {
            MsgUtils.error("%s not found", PATH_SERVICE_API);
            return;
        }

        DialogUtils.ModuleAndName moduleAndName = DialogUtils.getModuleAndName();
        if (moduleAndName == null) return;

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        String repositoryClassName = Arrays.stream(
                FilenameIndex.getFilesByName(project,
                        "I" + moduleAndName.getName() + "Repository.java",
                        GlobalSearchScope.projectScope(project)))
                .findFirst()
                .map(f -> (PsiJavaFile) f)
                .flatMap(f -> Arrays.stream(f.getClasses())
                        .findFirst()
                        .map(PsiClass::getQualifiedName))
                .orElse(null);

        Map<String, String> params = new HashMap<>();
        params.put("name", moduleAndName.getName());
        params.put("repositoryClassName", repositoryClassName);

        PsiFile interFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/Service.vm", params));
        interFile.setName(StringHelper.toString("{name}Service.java", params));

        PsiFile implFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/ServiceImpl.vm", params));
        implFile.setName(StringHelper.toString("{name}ServiceImpl.java", params));

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory moduleDir = directoryFactory.createDirectory(
                        FileUtils.findOrCreateDirectory(serviceApiFile, moduleAndName.getModulePath()));

                FileUtils.addIfAbsent(moduleDir, interFile);
                FileUtils.addIfAbsent(moduleDir, implFile);

                FileUtils.navigateFile(project, Optional
                        .ofNullable(moduleDir.findFile(interFile.getName()))
                        .map(PsiFile::getVirtualFile)
                        .orElse(null));
            } catch (IOException ex) {
                ex.printStackTrace();
                MsgUtils.error(ex.getMessage());
            }
        });
    }
}
