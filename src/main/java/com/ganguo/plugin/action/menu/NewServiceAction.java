package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MsgUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.PsiUtils;
import com.ganguo.plugin.util.StringHelper;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

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

        String prefix = Messages.showInputDialog("", "请输入前缀", null);
        if (StringUtils.isEmpty(prefix)) return;

        String moduleName;
        String name;

        int index = prefix.lastIndexOf(".");
        if (index == -1) {
            moduleName = prefix;
            name = prefix;
        } else {
            moduleName = prefix.substring(0, index);
            name = prefix.substring(index + 1);
        }

        if (StringUtils.isEmpty(moduleName)) {
            MsgUtils.error("模块名不能为空");
            return;
        }

        if (StringUtils.isEmpty(name)) {
            MsgUtils.error("名称不能为空");
            return;
        }

        moduleName = moduleName.toLowerCase();
        name = StringUtils.capitalize(name);

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        PsiDirectory serviceApiDir = directoryFactory.createDirectory(serviceApiFile);

        String repositoryClassName = Arrays.stream(
                FilenameIndex.getFilesByName(project, "I" + name + "Repository.java",
                        GlobalSearchScope.projectScope(project)))
                .findFirst()
                .map(f -> (PsiJavaFile) f)
                .flatMap(f -> Arrays.stream(f.getClasses())
                        .findFirst()
                        .map(PsiClass::getQualifiedName))
                .orElse(null);

        System.out.println(repositoryClassName);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("repositoryClassName", repositoryClassName);

        PsiFile interFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/Service.vm", params));
        interFile.setName(StringHelper.toString("{name}Service.java", params));

        PsiFile implFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/ServiceImpl.vm", params));
        implFile.setName(StringHelper.toString("{name}ServiceImpl.java", params));

        String finalModuleName = moduleName;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDirectory modelDir = serviceApiDir.findSubdirectory(finalModuleName);
            if (modelDir == null) {
                modelDir = serviceApiDir.createSubdirectory(finalModuleName);
            }

            PsiUtils.addIfAbsent(modelDir, interFile);
            PsiUtils.addIfAbsent(modelDir, implFile);

            FileUtils.navigateFile(project, Optional
                    .ofNullable(modelDir.findFile(interFile.getName()))
                    .map(PsiFile::getVirtualFile)
                    .orElse(null));
        });
    }
}
