package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.service.ProjectSettingService;
import com.ganguo.java.plugin.util.ElementUtils;
import com.ganguo.java.plugin.util.FilenameIndexUtils;
import com.ganguo.java.plugin.util.StringHelper;
import com.ganguo.java.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Ignore;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.util.Map;

public class JavaFileContext {

    /**
     * 当前文件
     */
    @Var
    private PsiJavaFile curFile(AnActionEvent event) {
        return (PsiJavaFile) event.getData(LangDataKeys.PSI_FILE);
    }

    /**
     * 当前方法
     */
    @Var
    private PsiMethod curMethod(AnActionEvent event) {
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        if (!ElementUtils.isMethodElement(psiElement)) {
            return null;
        }
        return (PsiMethod) psiElement;
    }

    /**
     * 当前文件的包名
     */
    @Var
    private String curPackageName(PsiJavaFile curFile) {
        return curFile.getPackageName();
    }

    /**
     * 根据文件名查找文件，只获取第一个文件
     */
    @Func
    private VirtualFile getVirtualFilesByName(Project project, @Ignore String name) {
        return FilenameIndexUtils.getVirtualFilesByName(project, name)
                .stream().findFirst().orElse(null);
    }

    /**
     * 根据文件名查找文件，只获取第一个
     */
    @Func
    private PsiFile getFilesByName(Project project, @Ignore String name) {
        PsiFile[] files = FilenameIndexUtils.getFilesByName(project, name);
        if (files != null && files.length > 0) {
            return files[0];
        }
        return null;
    }

    @Func
    private PsiFile createJavaFile(PsiFileFactory fileFactory, ProjectSettingService settingService,
                                   @Nla Map<String, Object> params,
                                   @Ignore TemplateName templateName, String filename) {
        PsiFile file = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(templateName), params));
        file.setName(StringHelper.toString(filename + ".java", params));
        return file;
    }
}
