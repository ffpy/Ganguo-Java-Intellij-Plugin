package com.ganguo.plugin.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class PsiUtils {

    public static void reformatJavaFile(PsiElement theElement) {
        WriteCommandAction.runWriteCommandAction(theElement.getProject(), () -> {
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(theElement.getProject());
            try {
                codeStyleManager.reformat(theElement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
