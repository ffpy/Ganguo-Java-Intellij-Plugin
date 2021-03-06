package com.ganguo.java.plugin.util;

import com.ganguo.java.plugin.constant.Filenames;
import com.ganguo.java.plugin.constant.Paths;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ProjectUtils {

    private static final String DEFAULT_PROJECT_NAME = "com.intellij.openapi.project.impl.DefaultProject";

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
                    log.error("get root dir fail!");
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
        return IndexUtils.getVirtualFilesByName(project, Filenames.APPLICATION)
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .map(VirtualFile::getPath)
                .map(path -> {
                    int index = path.lastIndexOf(Paths.PACKAGE_NAME_PATH_SEPARATE);
                    if (index == -1) return null;

                    return path.substring(index + Paths.PACKAGE_NAME_PATH_SEPARATE.length())
                            .replace('/', '.')
                            .replace('\\', '.');
                })
                .orElseGet(() -> {
                    log.error("get packageName fail!");
                    return null;
                });
    }

    /**
     * 获取包目录/src/main/java/com.xxx
     *
     * @param project project
     * @return 包目录
     */
    public static VirtualFile getPackageFile(Project project) {
        return IndexUtils.getVirtualFilesByName(project, Filenames.APPLICATION)
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    log.error("get package dir fail!");
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
        return IndexUtils.getVirtualFilesByName(project, Filenames.TEST_APPLICATION)
                .stream()
                .findFirst()
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    log.error("get test package dir fail!");
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
                .map(f -> f.findFileByRelativePath(Paths.RESOURCE))
                .orElseGet(() -> {
                    log.error("get resource dir fail!");
                    return null;
                });
    }

    /**
     * 获取多语言资源目录/src/main/resourse/i18n
     *
     * @param project project
     * @return 目录
     */
    public static VirtualFile getI18nDirFile(Project project) {
        return Optional.ofNullable(getResourceFile(project))
                .map(f -> f.findFileByRelativePath(Paths.I_18_N))
                .orElseGet(() -> {
                    log.error("get i18n dir fail!");
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
                    log.error("get test resource dir fail!");
                    return null;
                });
    }

    /**
     * 判断是不是打包插件时的Project
     *
     * @param project project
     * @return true为是，false为否
     */
    public static boolean isDefaultProject(Project project) {
        return project != null && project.getClass().getName().startsWith(DEFAULT_PROJECT_NAME);
    }
}
