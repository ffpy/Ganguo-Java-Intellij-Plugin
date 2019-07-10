package com.ganguo.plugin.action;

import com.ganguo.plugin.util.MsgUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.StringHelper;
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

public class NewRepositoryAction extends BaseAction {

    public static final String PATH_DOMAIN_REPOSITORY = "domain/repository";
    public static final String PATH_INFRASTRUCTURE_REPOSITORY = "infrastructure/repository";
    public static final String PATH_IMPL = "impl";
    public static final String PATH_DB_IMPL = "db/impl";

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

        String model;
        String name;

        int index = prefix.lastIndexOf(".");
        if (index == -1) {
            model = prefix;
            name = prefix;
        } else {
            model = prefix.substring(0, index);
            name = prefix.substring(index + 1);
        }

        if (StringUtils.isEmpty(model)) {
            MsgUtils.error("模块名不能为空");
            return;
        }

        if (StringUtils.isEmpty(name)) {
            MsgUtils.error("名称不能为空");
            return;
        }

        model = model.toLowerCase();
        name = StringUtils.capitalize(name);


        Map<String, Object> params = getParams(model, name, packageName);

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

        VirtualFile infrastructureRepositoryImplFile = infrastructureRepositoryFile.findFileByRelativePath(PATH_IMPL);
        if (infrastructureRepositoryImplFile == null) {
            MsgUtils.error("%s/%s not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_IMPL);
            return;
        }

        VirtualFile infrastructureRepositoryDbImplFile = infrastructureRepositoryFile.findFileByRelativePath(PATH_DB_IMPL);
        if (infrastructureRepositoryDbImplFile == null) {
            MsgUtils.error("%s/%s not found", PATH_INFRASTRUCTURE_REPOSITORY, PATH_DB_IMPL);
            return;
        }

        try {
            PsiDirectory domainRepositoryDir = directoryFactory
                    .createDirectory(domainRepositoryFile.createChildDirectory(project, model));

            PsiDirectory infrastructureRepositoryImplDir = directoryFactory
                    .createDirectory(infrastructureRepositoryImplFile);

            PsiDirectory infrastructureRepositoryDbImplDir = directoryFactory
                    .createDirectory(infrastructureRepositoryDbImplFile);

            PsiFile repositoryFile = fileFactory
                    .createFileFromText(JavaLanguage.INSTANCE, getRepositoryContent(params));
            repositoryFile.setName(StringHelper.of("I{name}Repository.java", params).toString());

            PsiFile dbStrategyFile = fileFactory
                    .createFileFromText(JavaLanguage.INSTANCE, getDbStrategyContent(params));
            dbStrategyFile.setName(StringHelper.of("I{name}DbStrategy.java", params).toString());

            PsiFile repositoryImplFile = fileFactory
                    .createFileFromText(JavaLanguage.INSTANCE, getRepositoryImplContent(params));
            repositoryImplFile.setName(StringHelper.of("{name}Repository.java", params).toString());

            PsiFile daoFile = fileFactory
                    .createFileFromText(JavaLanguage.INSTANCE, getDAOContent(params));
            daoFile.setName(StringHelper.of("{name}DAO.java", params).toString());

            WriteCommandAction.runWriteCommandAction(project, () -> {
                domainRepositoryDir.add(repositoryFile);
                domainRepositoryDir.add(dbStrategyFile);
                infrastructureRepositoryImplDir.add(repositoryImplFile);
                infrastructureRepositoryDbImplDir.add(daoFile);
            });
        } catch (IOException ex) {
            MsgUtils.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Map<String, Object> getParams(String model, String name, String packageName) {
        Map<String, Object> params = new HashMap<>();

        params.put("packageName", packageName);
        params.put("model", model);
        params.put("name", name);

        return params;
    }

    private String getRepositoryContent(Map<String, Object> params) {
        return StringHelper.of("public interface I{name}Repository {\n" +
                "    \n" +
                "}")
                .params(params)
                .toString();
    }

    private String getDbStrategyContent(Map<String, Object> params) {
        return StringHelper.of("import {packageName}.domain.repository.IDbStrategy;\n" +
                "\n" +
                "public interface I{name}DbStrategy extends IDbStrategy, I{name}Repository {\n" +
                "\n" +
                "}")
                .params(params)
                .toString();
    }

    private String getRepositoryImplContent(Map<String, Object> params) {
        return StringHelper.of("import {packageName}.domain.repository.{model}.I{name}DbStrategy;\n" +
                "import {packageName}.domain.repository.{model}.I{name}Repository;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.context.annotation.Primary;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "@Repository\n" +
                "@Primary\n" +
                "public class {name}Repository implements I{name}Repository {\n" +
                "\n" +
                "    @Autowired\n" +
                "    private I{name}DbStrategy m{name}DbStrategy;\n" +
                "\n" +
                "    \n" +
                "}\n")
                .params(params)
                .toString();
    }

    private String getDAOContent(Map<String, Object> params) {
        return StringHelper.of("import {packageName}.domain.repository.{model}.I{name}DbStrategy;\n" +
                "import {packageName}.infrastructure.repository.db.BaseDAO;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "@Repository\n" +
                "public class {name}DAO extends BaseDAO implements I{name}DbStrategy {\n" +
                "    \n" +
                "}")
                .params(params)
                .toString();
    }
}
