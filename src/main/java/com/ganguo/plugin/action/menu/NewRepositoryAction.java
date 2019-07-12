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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NewRepositoryAction extends BaseAction {

    private static final String PATH_DOMAIN_REPOSITORY = "domain/repository";
    private static final String PATH_INFRASTRUCTURE_REPOSITORY = "infrastructure/repository";
    private static final String PATH_IMPL = "impl";
    private static final String PATH_DB_IMPL = "db/impl";

    @Override
    public void action(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (noProject(project)) return;
        assert project != null;

        VirtualFile packageFile = ProjectUtils.getPackageFile(project);
        if (packageFile == null) return;

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        String packageName = ProjectUtils.getPackageName(project);
        if (packageName == null) return;

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


        VirtualFile domainRepositoryFile = packageFile.findFileByRelativePath(PATH_DOMAIN_REPOSITORY);
        if (domainRepositoryFile == null) {
            MsgUtils.error("%s not found", PATH_DOMAIN_REPOSITORY);
            return;
        }

        VirtualFile infrastructureRepositoryFile =
                packageFile.findFileByRelativePath(PATH_INFRASTRUCTURE_REPOSITORY);
        if (infrastructureRepositoryFile == null) {
            MsgUtils.error("%s not found", PATH_INFRASTRUCTURE_REPOSITORY);
            return;
        }

        VirtualFile infrastructureRepositoryImplFile =
                infrastructureRepositoryFile.findFileByRelativePath(PATH_IMPL);
        if (infrastructureRepositoryImplFile == null) {
            MsgUtils.error("%s/%s not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_IMPL);
            return;
        }

        VirtualFile infrastructureRepositoryDbImplFile =
                infrastructureRepositoryFile.findFileByRelativePath(PATH_DB_IMPL);
        if (infrastructureRepositoryDbImplFile == null) {
            MsgUtils.error("%s/%s not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_DB_IMPL);
            return;
        }

        Map<String, String> params = new HashMap<>();

        params.put("packageName", packageName);
        params.put("moduleName", moduleName);
        params.put("name", name);

        PsiFile repositoryFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/IRepository.vm", params));
        repositoryFile.setName(StringHelper.toString("I{name}Repository.java", params));

        PsiFile dbStrategyFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/IDbStrategy.vm", params));
        dbStrategyFile.setName(StringHelper.toString("I{name}DbStrategy.java", params));

        PsiFile repositoryImplFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/Repository.vm", params));
        repositoryImplFile.setName(StringHelper.toString("{name}Repository.java", params));

        PsiFile daoFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromResource("/template/DAO.vm", params));
        daoFile.setName(StringHelper.toString("{name}DAO.java", params));

        String finalModuleName = moduleName;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiDirectory domainRepositoryDir = directoryFactory
                        .createDirectory(FileUtils.findOrCreateDirectory(domainRepositoryFile, finalModuleName));

                PsiDirectory infrastructureRepositoryImplDir = directoryFactory
                        .createDirectory(infrastructureRepositoryImplFile);

                PsiDirectory infrastructureRepositoryDbImplDir = directoryFactory
                        .createDirectory(infrastructureRepositoryDbImplFile);

                PsiUtils.addIfAbsent(domainRepositoryDir, repositoryFile);
                PsiUtils.addIfAbsent(domainRepositoryDir, dbStrategyFile);
                PsiUtils.addIfAbsent(infrastructureRepositoryImplDir, repositoryImplFile);
                PsiUtils.addIfAbsent(infrastructureRepositoryDbImplDir, daoFile);

                FileUtils.navigateFile(project, Optional.ofNullable(
                        domainRepositoryDir.findFile(repositoryFile.getName()))
                        .map(PsiFile::getVirtualFile)
                        .orElse(null));
            } catch (IOException ex) {
                MsgUtils.error(ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
