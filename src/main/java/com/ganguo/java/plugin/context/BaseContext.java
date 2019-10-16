package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.service.SettingService;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.dependcode.dependcode.anno.Var;

public class BaseContext {

    @Var
    protected Project project(AnActionEvent event) {
        return event.getProject();
    }

    @Var
    protected VirtualFile rootFile(Project project) {
        return ProjectUtils.getRootFile(project);
    }

    @Var
    protected VirtualFile packageFile(Project project) {
        return ProjectUtils.getPackageFile(project);
    }

    @Var
    protected VirtualFile testPackageFile(Project project) {
        return ProjectUtils.getTestPackageFile(project);
    }

    @Var
    protected PsiDirectoryFactory directoryFactory(Project project) {
        return PsiDirectoryFactory.getInstance(project);
    }

    @Var
    protected PsiFileFactory fileFactory(Project project) {
        return PsiFileFactory.getInstance(project);
    }

    @Var
    protected SettingService settingService(Project project) {
        return ServiceManager.getService(project, SettingService.class);
    }

    @Var
    protected String packageName(SettingService settingService) {
        return settingService.getPackageName();
    }

    @Var
    protected PsiElementFactory elementFactory(Project project) {
        return JavaPsiFacade.getElementFactory(project);
    }

    @Var
    protected WriteActions writeActions(Project project) {
        return new WriteActions(project);
    }

    @Var
    protected Editor editor(AnActionEvent event) {
        return event.getData(LangDataKeys.HOST_EDITOR);
    }

    @Var
    protected PsiElement curElement(AnActionEvent event) {
        return event.getData(LangDataKeys.PSI_ELEMENT);
    }

    @Var
    protected PsiManager psiManager(Project project) {
        return PsiManager.getInstance(project);
    }
}
