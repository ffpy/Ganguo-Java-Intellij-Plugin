package com.ganguo.plugin.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class FileUtils {

    /**
     * 查找或者创建目录，支持嵌套创建
     *
     * @param base 基本路径
     * @param path 目录路径
     * @return 目录文件
     */
    public static VirtualFile findOrCreateDirectory(VirtualFile base, String path) throws IOException {
        if (base == null) return null;
        if (StringUtils.isEmpty(path)) return base;

        String[] names = StringUtils.split(path.replace('\\', '/'), "/");
        VirtualFile file = base;
        for (String name : names) {
            if (StringUtils.isEmpty(name)) continue;

            VirtualFile f = file.findChild(name);
            if (f != null) {
                file = f;
            } else {
                VirtualFile finalFile = file;
                file = ApplicationManager.getApplication().runWriteAction((Computable<VirtualFile>) () -> {
                    try {
                        return finalFile.createChildDirectory(base, name);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                });
            }
        }
        return file;
    }

    public static void navigateFile(Project project, VirtualFile file) {
        if (file == null) return;
        new OpenFileDescriptor(project, file).navigate(true);
    }

    public static void navigateFile(Project project, VirtualFile directory, String filename) {
        navigateFile(project, Optional.ofNullable(directory)
                .map(dir -> dir.findChild(filename))
                .orElse(null));
    }

    public static void navigateFile(Project project, PsiDirectory directory, String filename) {
        navigateFile(project, Optional.ofNullable(directory)
                .map(dir -> dir.findFile(filename))
                .map(PsiFile::getVirtualFile)
                .orElse(null));
    }

    /**
     * 如果文件存在则忽略，否则把文件添加到文件夹中
     *
     * @param directory 文件夹
     * @param file      文件
     * @return true为不存在，false为已存在
     */
    public static boolean addIfAbsent(PsiDirectory directory, PsiFile file) {
        if (directory.findFile(file.getName()) != null) {
            MsgUtils.info("%s已存在", file.getName());
            return false;
        }
        directory.add(file);
        return true;
    }

    /**
     * 修改文件的内容
     *
     * @param file       文件
     * @param properties 配置文件
     */
    public static void setContent(VirtualFile file, SafeProperties properties) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        properties.store(bos, null);
        setContent(file, bos.toByteArray());
    }

    /**
     * 修改文件的内容
     *
     * @param file    文件
     * @param content 内容
     */
    public static void setContent(VirtualFile file, byte[] content) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                file.setBinaryContent(content);
                file.refresh(true, false);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
