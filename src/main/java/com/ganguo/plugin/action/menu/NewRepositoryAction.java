package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.constant.Paths;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.ui.dialog.NewRepositoryDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MyStringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.CodeContextBuilder;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 创建Repository接口及实现类
 */
@Slf4j
public class NewRepositoryAction extends NewAction {

    @Override
    public void action(@NotNull AnActionEvent e) {
        new NewRepositoryDialog(e, "New Repository", this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String table, String module, String name) {
        return CodeContextBuilder.of(this)
                .put("event", event)
                .put("table", table)
                .put("module", module)
                .put("name", name)
                .build()
                .execVoid("writeFile")
                .isPresent();
    }

    /**
     * 写入对应的文件
     */
    @Func
    private void writeFile(Project project, PsiDirectory domainDir, PsiDirectory infrastructureImplDir,
                           PsiDirectory infrastructureDbImplDir, PsiFile repositoryFile,
                           PsiFile dbStrategyFile, PsiFile repositoryImplFile, PsiFile daoFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            FileUtils.addIfAbsent(domainDir, repositoryFile);
            FileUtils.addIfAbsent(domainDir, dbStrategyFile);
            FileUtils.addIfAbsent(infrastructureImplDir, repositoryImplFile);
            FileUtils.addIfAbsent(infrastructureDbImplDir, daoFile);

            FileUtils.navigateFile(project, domainDir, repositoryFile.getName());
        });
    }

    /**
     * 表的POJO名称
     */
    @Var
    private String pojo(String table) {
        return StringUtils.capitalize(MyStringUtils.underScoreCase2CamelCase(table.toLowerCase()));
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String packageName, String module, String name, String table, String pojo) {
        Map<String, Object> params = new HashMap<>();

        params.put("packageName", packageName);
        params.put("moduleName", module);
        params.put("name", name);
        params.put("table", table);
        params.put("pojoCls", pojo + "POJO");
        params.put("pojoName", MyStringUtils.lowerCaseFirstChar(pojo));

        return params;
    }

    /**
     * IRepository接口文件
     */
    @Var
    private PsiFile repositoryFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.I_REPOSITORY, "I{name}Repository").get();
    }

    /**
     * IDbStrategy接口文件
     */
    @Var
    private PsiFile dbStrategyFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.I_DB_STRATEGY, "I{name}DbStrategy").get();
    }

    /**
     * Repository文件
     */
    @Var
    private PsiFile repositoryImplFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.REPOSITORY, "{name}Repository").get();
    }

    /**
     * DAO文件
     */
    @Var
    private PsiFile daoFile(Context context) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.DAO, "{name}DAO").get();
    }

    /**
     * domain文件夹
     */
    @Var
    private PsiDirectory domainDir(Context context) {
        return context.exec("createModuleDir", PsiDirectory.class, Paths.DOMAIN_REPOSITORY).get();
    }

    /**
     * Repository所在的文件夹
     */
    @Var
    private PsiDirectory infrastructureImplDir(PsiDirectoryFactory directoryFactory,
                                               VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }

    /**
     * DAO所在的文件夹
     */
    @Var
    private PsiDirectory infrastructureDbImplDir(PsiDirectoryFactory directoryFactory,
                                                 VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_DB_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }
}
