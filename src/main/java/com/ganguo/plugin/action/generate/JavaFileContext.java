package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.util.ElementUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Var;

public class JavaFileContext {
    private static final Context CONTEXT = ContextBuilder.of(new JavaFileContext()).build();

    public static Context getContext() {
        return CONTEXT;
    }

    /**
     * 当前文件
     */
    @Var
    private PsiJavaFile curFile(AnActionEvent event) {
        return (PsiJavaFile) event.getData(LangDataKeys.PSI_FILE);
    }

    /**
     * 当前方法
     */
    @Var
    private PsiMethod curMethod(AnActionEvent event) {
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        if (!ElementUtils.isMethodElement(psiElement)) {
            return null;
        }
        return (PsiMethod) psiElement;
    }

    /**
     * 当前文件的包名
     */
    @Var
    private String curPackageName(PsiJavaFile curFile) {
        return curFile.getPackageName();
    }
}
