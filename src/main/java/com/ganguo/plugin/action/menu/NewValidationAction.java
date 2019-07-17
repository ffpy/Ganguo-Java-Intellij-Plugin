package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.service.ProjectSettingService;
import com.ganguo.plugin.ui.dialog.ModuleAndNameDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.StringHelper;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 创建校验注解及校验类
 */
@Slf4j
public class NewValidationAction extends BaseAction {

    private static final String PATH_VALIDATION = "infrastructure/validation/";

    private AnActionEvent mEvent;

    @Override
    protected void action(AnActionEvent e) throws Exception {
        mEvent = e;
        try {
            new ModuleAndNameDialog("New Validation", this::doAction).show();
        } finally {
            mEvent = null;
        }
    }

    private boolean doAction(String module, String name) {
        Project project = mEvent.getProject();
        if (noProject(project)) return false;
        assert project != null;

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        ProjectSettingService settingService =
                ServiceManager.getService(project, ProjectSettingService.class);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("packageName", settingService.getPackageName());

        PsiFile validationFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.VALIDATION),
                        params));
        validationFile.setName(StringHelper.toString("{name}.java", params));

        PsiFile validationImplFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.VALIDATION_IMPL),
                        params));
        validationImplFile.setName(StringHelper.toString("{name}ValidatorImpl.java", params));

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory moduleDir = directoryFactory.createDirectory(
                        FileUtils.findOrCreateDirectory(ProjectUtils.getPackageFile(project),
                                PATH_VALIDATION + module.replace('.', '/')));

                FileUtils.addIfAbsent(moduleDir, validationFile);
                FileUtils.addIfAbsent(moduleDir, validationImplFile);

                FileUtils.navigateFile(project, Optional
                        .ofNullable(moduleDir.findFile(validationFile.getName()))
                        .map(PsiFile::getVirtualFile)
                        .orElse(null));

                FileUtils.navigateFile(project, Optional
                        .ofNullable(moduleDir.findFile(validationImplFile.getName()))
                        .map(PsiFile::getVirtualFile)
                        .orElse(null));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        });
        return true;
    }
}
