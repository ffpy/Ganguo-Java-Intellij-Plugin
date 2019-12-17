package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.ImportFrom;
import org.jetbrains.annotations.NotNull;

@ImportFrom(JavaFileContext.class)
abstract class BaseSortAction extends BaseAnAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .isJavaFile()
                .update();
    }

    protected int getOrder(PsiModifierList modifierList) {
        if (modifierList == null) {
            return 0;
        }
        int order = 0;

        if (modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) {
            order += 10000;
        }
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            order += 1000;
        }
        if (modifierList.hasModifierProperty(PsiModifier.FINAL)) {
            order += 100;
        }

        // public protected default private
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            order += 10;
        } else if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) {
            order += 9;
        } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            order += 7;
        } else {
            order += 8;
        }

        return order;
    }
}
