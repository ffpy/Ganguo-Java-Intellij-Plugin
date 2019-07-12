package com.ganguo.plugin.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class ElementUtils {

    public static boolean isMethodElement(PsiElement element) {
        if (element == null) return false;
        return element instanceof PsiMethod;
    }
}
