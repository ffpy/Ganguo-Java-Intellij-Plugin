package com.ganguo.java.plugin.action;

import com.ganguo.java.plugin.service.ProjectSettingService;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public abstract class BaseAction extends AnAction implements DumbAware {

    protected abstract void action(AnActionEvent e) throws Exception;

    @Override
    @Deprecated
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            action(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
    }

    /**
     * 当文件名匹配时显示
     */
    protected void showWithFilename(AnActionEvent e, Function<String, Boolean> f) {
        boolean show = Optional.ofNullable(e.getData(LangDataKeys.PSI_FILE))
                .filter(file -> e.getProject() != null)
                .map(PsiFileSystemItem::getName)
                .map(f)
                .orElse(false);
        e.getPresentation().setEnabled(show);
    }

    protected void showWithAnnotationOnClass(AnActionEvent e, String annotationName) {
        boolean show = Optional.ofNullable(e.getData(LangDataKeys.PSI_FILE))
                .map(file -> (PsiJavaFile) file)
                .filter(file -> e.getProject() != null)
                .map(PsiUtils::getClassByFile)
                .map(cls -> cls.getAnnotation(annotationName) != null)
                .orElse(false);
        e.getPresentation().setEnabled(show);
    }

    @Var
    private Project project(AnActionEvent event) {
        return event.getProject();
    }

    @Var
    private VirtualFile rootFile(Project project) {
        return ProjectUtils.getRootFile(project);
    }

    @Var
    private VirtualFile packageFile(Project project) {
        return ProjectUtils.getPackageFile(project);
    }

    @Var
    private VirtualFile testPackageFile(Project project) {
        return ProjectUtils.getTestPackageFile(project);
    }

    @Var
    private PsiDirectoryFactory directoryFactory(Project project) {
        return PsiDirectoryFactory.getInstance(project);
    }

    @Var
    private PsiFileFactory fileFactory(Project project) {
        return PsiFileFactory.getInstance(project);
    }

    @Var
    private ProjectSettingService settingService(Project project) {
        return ServiceManager.getService(project, ProjectSettingService.class);
    }

    @Var
    private String packageName(ProjectSettingService settingService) {
        return settingService.getPackageName();
    }

    @Var
    private PsiElementFactory elementFactory(Project project) {
        return JavaPsiFacade.getElementFactory(project);
    }

    @Var(cache = false)
    private WriteActions writeActions(Project project) {
        return new WriteActions(project);
    }
}
