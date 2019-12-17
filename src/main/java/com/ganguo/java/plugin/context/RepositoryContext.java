package com.ganguo.java.plugin.context;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.ImportThose;
import org.dependcode.dependcode.anno.Var;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ImportThose(value = JavaFileContext.class, data = "getFilesByName")
public class RepositoryContext {

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
