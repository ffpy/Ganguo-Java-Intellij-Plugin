package com.ganguo.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Optional;

public class ProjectUtils {

    private static final String PACKAGE_NAME_PATH_SEPARATE = "/java/";
    private static final String APPLICATION_FILENAME = "Application.java";
    private static final String TEST_APPLICATION_FILENAME = "ApplicationTests.java";

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
                .orElse(null);
    }

    /**
     * 获取包目录/src/main/java/com.xxx
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
     * 获取测试的包目录/src/test/java/com.xxx
     *
     * @param project project
     * @return 测试的包目录
     */
    public static VirtualFile getTestPackageFile(Project project) {
        return FilenameIndex
                .getVirtualFilesByName(project, TEST_APPLICATION_FILENAME,
                        GlobalSearchScope.projectScope(project))
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    MsgUtils.error("get test package file fail!");
                    return null;
                });
    }

    /**
     * 获取资源目录/src/main/resource
     *
     * @param project project
     * @return 资源目录
     */
    public static VirtualFile getResourceFile(Project project) {
        return Optional.ofNullable(getRootFile(project))
                .map(f -> f.findFileByRelativePath("src/main/resources"))
                .orElseGet(() -> {
                    MsgUtils.error("get resource file fail!");
                    return null;
                });
    }

    /**
     * 获取测试资源目录/src/test/resource
     *
     * @param project project
     * @return 测试资源目录
     */
    public static VirtualFile getTestResourceFile(Project project) {
        return Optional.ofNullable(getRootFile(project))
                .map(f -> f.findFileByRelativePath("src/test/resources"))
                .orElseGet(() -> {
                    MsgUtils.error("get test resource file fail!");
                    return null;
                });
    }
}
