package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.context.JavaFileContext;
import com.ganguo.plugin.context.NewContext;
import com.ganguo.plugin.context.RepositoryContext;
import com.ganguo.plugin.ui.dialog.NewRepositoryDialog;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MyStringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建Repository接口及实现类
 */
@Slf4j
public class NewRepositoryAction extends BaseAction {

    @Override
    public void action(@NotNull AnActionEvent e) {
        new NewRepositoryDialog(e, "New Repository", this::doAction).show();
    }

    private boolean doAction(AnActionEvent event, String table, String module, String name) {
        return ContextBuilder.of(this)
                .put("event", event)
                .put("table", table)
                .put("module", module)
                .put("name", name)
                .importFrom(NewContext.getContext())
                .importFrom(RepositoryContext.getContext())
                .importFrom(JavaFileContext.getContext())
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
    private PsiFile repositoryFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.I_REPOSITORY, "I{name}Repository");
    }

    /**
     * IDbStrategy接口文件
     */
    @Var
    private PsiFile dbStrategyFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.I_DB_STRATEGY, "I{name}DbStrategy");
    }

    /**
     * Repository文件
     */
    @Var
    private PsiFile repositoryImplFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.REPOSITORY, "{name}Repository");
    }

    /**
     * DAO文件
     */
    @Var
    private PsiFile daoFile(FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.DAO, "{name}DAO");
    }
}
