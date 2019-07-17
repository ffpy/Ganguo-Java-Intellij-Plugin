package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.service.ProjectSettingService;
import com.ganguo.plugin.ui.dialog.NewRepositoryDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MyStringUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.StringHelper;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 创建Repository接口及实现类
 */
@Slf4j
public class NewRepositoryAction extends BaseAction {

    private static final String PATH_DOMAIN_REPOSITORY = "domain/repository";
    private static final String PATH_INFRASTRUCTURE_REPOSITORY = "infrastructure/repository";
    private static final String PATH_IMPL = "impl";
    private static final String PATH_DB_IMPL = "db/impl";

    private AnActionEvent mEvent;

    @Override
    public void action(@NotNull AnActionEvent e) {
        mEvent = e;
        try {
            new NewRepositoryDialog("New Repository", this::doAction).show();
        } finally {
            mEvent = null;
        }
    }

    private boolean doAction(String table, String module, String name) {
        Project project = mEvent.getProject();

        if (noProject(project)) return false;
        assert project != null;

        VirtualFile packageFile = ProjectUtils.getPackageFile(project);
        if (packageFile == null) return false;

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        ProjectSettingService settingService =
                ServiceManager.getService(project, ProjectSettingService.class);

        String packageName = settingService.getPackageName();
        if (packageName == null) return false;

        VirtualFile domainRepositoryFile = packageFile.findFileByRelativePath(PATH_DOMAIN_REPOSITORY);
        if (domainRepositoryFile == null) {
            log.error("{} not found", PATH_DOMAIN_REPOSITORY);
            return false;
        }

        VirtualFile infrastructureRepositoryFile =
                packageFile.findFileByRelativePath(PATH_INFRASTRUCTURE_REPOSITORY);
        if (infrastructureRepositoryFile == null) {
            log.error("{} not found", PATH_INFRASTRUCTURE_REPOSITORY);
            return false;
        }

        VirtualFile infrastructureRepositoryImplFile =
                infrastructureRepositoryFile.findFileByRelativePath(PATH_IMPL);
        if (infrastructureRepositoryImplFile == null) {
            log.error("{}/{} not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_IMPL);
            return false;
        }

        VirtualFile infrastructureRepositoryDbImplFile =
                infrastructureRepositoryFile.findFileByRelativePath(PATH_DB_IMPL);
        if (infrastructureRepositoryDbImplFile == null) {
            log.error("{}/{} not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_DB_IMPL);
            return false;
        }

        String pojo = StringUtils.capitalize(MyStringUtils.underScoreCase2CamelCase(table.toLowerCase()));

        Map<String, String> params = new HashMap<>();

        params.put("packageName", packageName);
        params.put("moduleName", module);
        params.put("name", name);
        params.put("table", table);
        params.put("pojoCls", pojo + "POJO");
        params.put("pojoName", MyStringUtils.lowerCaseFirstChar(pojo));

        PsiFile repositoryFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.I_REPOSITORY),
                        params));
        repositoryFile.setName(StringHelper.toString("I{name}Repository.java", params));

        PsiFile dbStrategyFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.I_DB_STRATEGY),
                        params));
        dbStrategyFile.setName(StringHelper.toString("I{name}DbStrategy.java", params));

        PsiFile repositoryImplFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.REPOSITORY),
                        params));
        repositoryImplFile.setName(StringHelper.toString("{name}Repository.java", params));

        PsiFile daoFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.DAO),
                        params));
        daoFile.setName(StringHelper.toString("{name}DAO.java", params));

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory domainRepositoryDir = directoryFactory.createDirectory(
                        FileUtils.findOrCreateDirectory(domainRepositoryFile,
                                module.replace('.', '/')));

                PsiDirectory infrastructureRepositoryImplDir = directoryFactory
                        .createDirectory(infrastructureRepositoryImplFile);

                PsiDirectory infrastructureRepositoryDbImplDir = directoryFactory
                        .createDirectory(infrastructureRepositoryDbImplFile);

                FileUtils.addIfAbsent(domainRepositoryDir, repositoryFile);
                FileUtils.addIfAbsent(domainRepositoryDir, dbStrategyFile);
                FileUtils.addIfAbsent(infrastructureRepositoryImplDir, repositoryImplFile);
                FileUtils.addIfAbsent(infrastructureRepositoryDbImplDir, daoFile);

                FileUtils.navigateFile(project, Optional.ofNullable(
                        domainRepositoryDir.findFile(repositoryFile.getName()))
                        .map(PsiFile::getVirtualFile)
                        .orElse(null));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        });
        return true;
    }
}
