package com.ganguo.java.plugin.action;

import com.ganguo.java.plugin.context.BaseContext;
import com.ganguo.java.plugin.service.ProjectSettingService;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@ForceImportFrom(BaseContext.class)
public abstract class BaseAnAction extends AnAction implements DumbAware {

    protected AnActionEvent mEvent;

    protected abstract void action(AnActionEvent e) throws Exception;

    @Override
    @Deprecated
    public void actionPerformed(@NotNull AnActionEvent event) {
        this.mEvent = event;
        try {
            action(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            this.mEvent = null;
        }
    }
}
