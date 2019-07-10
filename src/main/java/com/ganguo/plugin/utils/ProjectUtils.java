package com.ganguo.plugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Optional;

public class ProjectUtils {

    private static final String PACKAGE_NAME_PATH_SEPARATE = "/java/";
    private static final String APPLICATION_FILENAME = "Application.java";
    private static final String APPLICATION_PROPERTIES_FILENAME = "application.properties";

    /**
     * 获取项目的根目录
     *
     * @param project project
     * @return 根目录
     */
    public static VirtualFile getRootFile(Project project) {
        return Optional.ofNullable(project)
                .map(Project::getProjectFile)
                .map(VirtualFile::getParent)
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    MsgUtils.error("get root file fail!");
                    return null;
                });
    }

    /**
     * 获取项目的包名
     *
     * @param project project
     * @return 包名
     */
    public static String getPackageName(Project project) {
        return FilenameIndex
                .getVirtualFilesByName(project, APPLICATION_FILENAME,
                        GlobalSearchScope.projectScope(project))
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .map(VirtualFile::getPath)
                .map(path -> {
                    int index = path.lastIndexOf(PACKAGE_NAME_PATH_SEPARATE);
                    if (index == -1) return null;

                    return path.substring(index + PACKAGE_NAME_PATH_SEPARATE.length())
                            .replace('/', '.')
                            .replace('\\', '.');
                })
                .orElseGet(() -> {
                    MsgUtils.error("get packageName fail!");
                    return "";
                });
    }

    /**
     * 获取包目录
     *
     * @param project project
     * @return 包目录
     */
    public static VirtualFile getPackageFile(Project project) {
        return FilenameIndex
                .getVirtualFilesByName(project, APPLICATION_FILENAME,
                        GlobalSearchScope.projectScope(project))
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    MsgUtils.error("get package file fail!");
                    return null;
                });
    }

    /**
     * 获取资源目录
     *
     * @param project project
     * @return 资源目录
     */
    public static VirtualFile getResourceFile(Project project) {
        return FilenameIndex
                .getVirtualFilesByName(project, APPLICATION_PROPERTIES_FILENAME,
                        GlobalSearchScope.projectScope(project))
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    MsgUtils.error("get resource file fail!");
                    return null;
                });
    }
}
