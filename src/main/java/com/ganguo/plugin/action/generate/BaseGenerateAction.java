package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.action.BaseAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class BaseGenerateAction extends BaseAction {

    protected abstract boolean isShow(AnActionEvent e);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isShow(e));
    }

    protected boolean checkShow(AnActionEvent e, String pattern, Predicate<PsiElement> elementChecker) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            return false;
        }

        PsiJavaFile curFile = (PsiJavaFile) psiFile;

        String qualifiedName = Optional.of(curFile)
                .flatMap(file -> Arrays.stream(file.getClasses()).findFirst())
                .map(PsiClass::getQualifiedName)
                .orElse(null);

        return Optional.of(curFile)
                .map(PsiFile::getVirtualFile)
                .map(VirtualFile::getName)
                .filter(name -> name.matches(pattern))
                .map(it -> e.getData(LangDataKeys.PSI_ELEMENT))
                .map(element -> {
                    if (!(element instanceof PsiMethod)) {
                        return false;
                    }
                    PsiElement parent = element.getParent();
                    if (!(parent instanceof PsiClass)) {
                        return false;
                    }
                    if (!Objects.equals(((PsiClass) parent).getQualifiedName(), qualifiedName)) {
                        return false;
                    }
                    return true;

                })
                .orElse(false);
    }
}
