package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.action.BaseAction;
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

public abstract class BaseGenerateAction extends BaseAction {

    protected abstract boolean isShow(AnActionEvent e);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isShow(e));
    }
}
