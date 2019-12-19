package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.ImportFrom;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

    protected boolean hasModifierProperty(PsiModifierListOwner owner, String name) {
        return Optional.ofNullable(owner.getModifierList())
                .map(list -> list.hasModifierProperty(name))
                .orElse(false);
    }

    protected int getAccessOrder(PsiModifierListOwner owner) {
        if (hasModifierProperty(owner, PsiModifier.PUBLIC)) {
            return 4;
        }
        if (hasModifierProperty(owner, PsiModifier.PROTECTED)) {
            return 3;
        }
        if (hasModifierProperty(owner, PsiModifier.PRIVATE)) {
            return 1;
        }
        return 2;
    }
}
