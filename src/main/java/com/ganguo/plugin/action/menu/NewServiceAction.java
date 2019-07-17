package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.service.ProjectSettingService;
import com.ganguo.plugin.ui.dialog.ModuleAndNameDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.FilenameIndexUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.StringHelper;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 创建Service接口及实现类
 */
@Slf4j
public class NewServiceAction extends BaseAction {

    public static final String PATH_SERVICE_API = "service/api";

    private AnActionEvent mEvent;

    @Override
    public void action(@NotNull AnActionEvent e) {
        mEvent = e;
        try {
            new ModuleAndNameDialog("New Service", this::doAction).show();
        } finally {
            mEvent = null;
        }
    }

    private boolean doAction(String module, String name) {
        Project project = mEvent.getProject();

        if (noProject(project)) return false;
        assert project != null;

        VirtualFile packageFile = ProjectUtils.getPackageFile(project);
        if (packageFile == null) return false;

        VirtualFile serviceApiFile = packageFile.findFileByRelativePath(PATH_SERVICE_API);
        if (serviceApiFile == null) {
            log.error("{} not found", PATH_SERVICE_API);
            return false;
        }

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        String repositoryClassName = Arrays.stream(
                FilenameIndexUtils.getFilesByName(project,
                        "I" + name + "Repository.java"))
                .findFirst()
                .map(f -> (PsiJavaFile) f)
                .flatMap(f -> Arrays.stream(f.getClasses())
                        .findFirst()
                        .map(PsiClass::getQualifiedName))
                .orElse(null);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("repositoryClassName", repositoryClassName);

        ProjectSettingService settingService =
                ServiceManager.getService(project, ProjectSettingService.class);

        PsiFile interFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.SERVICE),
                        params));
        interFile.setName(StringHelper.toString("{name}Service.java", params));

        PsiFile implFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.SERVICE_IMPL),
                        params));
        implFile.setName(StringHelper.toString("{name}ServiceImpl.java", params));

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory moduleDir = Optional.ofNullable(
                        FileUtils.findOrCreateDirectory(serviceApiFile,
                                module.replace('.', '/')))
                        .map(directoryFactory::createDirectory)
                        .orElse(null);
                if (moduleDir == null) {
                    log.error("get module dir fail");
                    return;
                }

                FileUtils.addIfAbsent(moduleDir, interFile);
                FileUtils.addIfAbsent(moduleDir, implFile);

                FileUtils.navigateFile(project, moduleDir, interFile.getName());
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        });
        return true;
    }
}
