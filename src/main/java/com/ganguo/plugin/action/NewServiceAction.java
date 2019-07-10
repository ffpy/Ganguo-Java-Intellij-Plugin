package com.ganguo.plugin.action;

import com.ganguo.plugin.utils.MsgUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 位于右键New
 */
public class NewServiceAction extends BaseAction {

    @Override
    public void action(@NotNull AnActionEvent e) {
        VirtualFile selectedDir = getDir(e.getData(DataKeys.VIRTUAL_FILE));
        if (selectedDir == null) {
            return;
        }

        String prefix = Messages.showInputDialog("", "请输入前缀", null);

        if (StringUtils.isEmpty(prefix)) {
            return;
        }

        prefix = StringUtils.capitalize(prefix);

        Project project = e.getProject();

        VirtualFile projectFile = Optional.ofNullable(project)
                .map(Project::getProjectFile)
                .map(VirtualFile::getParent)
                .map(VirtualFile::getParent)
                .orElse(null);

        if (projectFile == null) {
            MsgUtils.error("get projectFile fail!");
            return;
        }

        String projectFilePath = projectFile.getPath();
        String selectedDirPath = selectedDir.getPath();
        if (!selectedDirPath.startsWith(projectFilePath)) {
            MsgUtils.warn("非法路径！");
            return;
        }

        PsiFileFactory factory = PsiFileFactory.getInstance(project);

        PsiFile interPsi = factory.createFileFromText(
                JavaLanguage.INSTANCE, getInterfaceContent(prefix));
        interPsi.setName(prefix + "Service.java");


        PsiFile implPsi = factory.createFileFromText(
                JavaLanguage.INSTANCE, getImplContent(prefix));
        implPsi.setName(prefix + "ServiceImpl.java");

        PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(selectedDir);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            directory.add(interPsi);
            directory.add(implPsi);
        });
    }

    private String getInterfaceContent(String prefix) {
        return String.format(
                "public interface %sService {\n" +
                        "    \n" +
                        "}\n", prefix);
    }

    private String getImplContent(String prefix) {
        return String.format(
                "import lombok.extern.slf4j.Slf4j;\n" +
                        "import org.springframework.stereotype.Service;\n" +
                        "\n" +
                        "@Slf4j\n" +
                        "@Service\n" +
                        "public class %sServiceImpl implements %sService {\n" +
                        "    \n" +
                        "}", prefix, prefix);
    }

    private VirtualFile getDir(VirtualFile file) {
        if (file == null) {
            return null;
        }

        if (!file.isDirectory()) {
            file = file.getParent();
        }

        return file;
    }
}
