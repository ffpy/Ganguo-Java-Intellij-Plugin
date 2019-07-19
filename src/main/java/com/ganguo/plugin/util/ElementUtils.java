package com.ganguo.plugin.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class ElementUtils {

    public static boolean isMethodElement(PsiElement element) {
        return element instanceof PsiMethod;
    }
}
