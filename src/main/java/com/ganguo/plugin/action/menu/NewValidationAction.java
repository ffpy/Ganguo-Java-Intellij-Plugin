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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NewValidationAction extends BaseAction {

    private static final String PATH_VALIDATION = "infrastructure/validation/";

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Project project = e.getProject();
        if (noProject(project)) return;
        assert project != null;

        DialogUtils.ModuleAndName moduleAndName = DialogUtils.getModuleAndName();
        if (moduleAndName == null) return;

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        Map<String, String> params = new HashMap<>();
        params.put("name", moduleAndName.getName());
        params.put("packageName", ProjectUtils.getPackageName(project));

        PsiFile validationFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/Validation.vm", params));
        validationFile.setName(StringHelper.toString("{name}.java", params));

        PsiFile validationImplFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/ValidationImpl.vm", params));
        validationImplFile.setName(StringHelper.toString("{name}ValidatorImpl.java", params));

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory moduleDir = directoryFactory.createDirectory(FileUtils.findOrCreateDirectory(
                        ProjectUtils.getPackageFile(project), PATH_VALIDATION + moduleAndName.getModulePath()));

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
                ex.printStackTrace();
                MsgUtils.error(ex.getMessage());
            }
        });
    }
}
