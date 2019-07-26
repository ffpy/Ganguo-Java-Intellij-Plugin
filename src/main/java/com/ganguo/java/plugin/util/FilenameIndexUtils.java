package com.ganguo.java.plugin.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Collection;

public class FilenameIndexUtils {

    public static Collection<VirtualFile> getVirtualFilesByName(Project project, String name) {
        return getVirtualFilesByName(project, name, GlobalSearchScope.projectScope(project));
    }

    public static Collection<VirtualFile> getVirtualFilesByName(
            Project project, String name, GlobalSearchScope scope) {
        return ApplicationManager.getApplication().runReadAction((Computable<Collection<VirtualFile>>) () ->
                FilenameIndex.getVirtualFilesByName(project, name, scope));
    }

    public static PsiFile[] getFilesByName(Project project, String name) {
        return getFilesByName(project, name, GlobalSearchScope.projectScope(project));
    }

    public static PsiFile[] getFilesByName(Project project, String name, GlobalSearchScope scope) {
        return ApplicationManager.getApplication().runReadAction((Computable<PsiFile[]>) () ->
                FilenameIndex.getFilesByName(project, name, scope));
    }
}
