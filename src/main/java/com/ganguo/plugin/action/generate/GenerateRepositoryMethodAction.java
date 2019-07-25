package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.context.JavaFileContext;
import com.ganguo.plugin.util.FileUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GenerateRepositoryMethodAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .importAll(JavaFileContext.getContext())
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return isMethodOfClass(e, "^I.*Repository.java$");
    }

    @Func
    private void doAction(Project project, PsiMethod curMethod,
                          PsiClass daoClass, PsiClass implClass,
                          FuncAction<PsiMethod> createMethodOnDao,
                          FuncAction<PsiMethod> createMethodOnImpl) {
        if (daoClass.findMethodBySignature(curMethod, false) == null) {
            createMethodOnDao.exec().getOptional()
                    .ifPresent(method -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        daoClass.add(method);
                    }));
        }
        if (implClass.findMethodBySignature(curMethod, false) == null) {
            createMethodOnImpl.exec().getOptional()
                    .ifPresent(method -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        implClass.add(method);
                    }));
        }
        FileUtils.navigateFile(project, implClass.getContainingFile().getVirtualFile());
    }

    @Func
    private PsiMethod createMethodOnDao(PsiMethod curMethod) {
        return (PsiMethod) curMethod.copy();
    }

    @Func
    private PsiMethod createMethodOnImpl(PsiMethod curMethod) {
        return (PsiMethod) curMethod.copy();
    }

    @Var
    private String moduleName(PsiJavaFile curFile) {
        Matcher matcher = Pattern.compile("I(.*)Repository.java").matcher(curFile.getName());
        return matcher.find() ? StringUtils.capitalize(matcher.group(1)) : null;
    }

    @Var
    private PsiJavaFile daoFile(String moduleName, FuncAction<PsiFile> getFilesByName) {
        PsiFile file = getFilesByName.get(moduleName + "DAO.java");
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    @Var
    private PsiJavaFile implFile(String moduleName, FuncAction<PsiFile> getFilesByName) {
        PsiFile file = getFilesByName.get(moduleName + "Repository.java");
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    @Var
    private PsiClass daoClass(PsiJavaFile daoFile) {
        return Arrays.stream(daoFile.getClasses()).findFirst().orElse(null);
    }

    @Var
    private PsiClass implClass(PsiJavaFile implFile) {
        return Arrays.stream(implFile.getClasses()).findFirst().orElse(null);
    }
}
