package com.ganguo.plugin.action;

import com.ganguo.plugin.utils.MsgUtils;
import com.ganguo.plugin.utils.ProjectUtils;
import com.ganguo.plugin.utils.StringHelper;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
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

/**
 * 位于顶部菜单栏
 */
public class NewService2Action extends BaseAction {

    public static final String PATH_SERVICE_API = "service/api";

    @Override
    public void action(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (noProject(project)) return;
        assert project != null;

        VirtualFile packageFile = ProjectUtils.getPackageFile(project);
        if (packageFile == null) return;

        VirtualFile serviceApiFile = packageFile.findFileByRelativePath(PATH_SERVICE_API);
        if (serviceApiFile == null) {
            MsgUtils.error("%s not found", PATH_SERVICE_API);
            return;
        }

        String prefix = Messages.showInputDialog("", "请输入前缀", null);
        if (StringUtils.isEmpty(prefix)) return;

        String model;
        String name;

        int index = prefix.lastIndexOf(".");
        if (index == -1) {
            model = prefix;
            name = prefix;
        } else {
            model = prefix.substring(0, index);
            name = prefix.substring(index + 1);
        }

        if (StringUtils.isEmpty(model)) {
            MsgUtils.error("模块名不能为空");
            return;
        }

        if (StringUtils.isEmpty(name)) {
            MsgUtils.error("名称不能为空");
            return;
        }

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        PsiDirectory serviceApiDir = directoryFactory.createDirectory(serviceApiFile);
        PsiDirectory modelDir = serviceApiDir.findSubdirectory(model);
        if (modelDir == null) {
            modelDir = serviceApiDir.createSubdirectory(model);
        }

        PsiFile interFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE, getInterfaceContent(name));
        interFile.setName(StringHelper.of("{name}Service.java").param("name", name).toString());

        PsiFile implFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE, getImplContent(name));
        implFile.setName(StringHelper.of("{name}ServiceImpl.java").param("name", name).toString());

        PsiDirectory finalModelDir = modelDir;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            finalModelDir.add(interFile);
            finalModelDir.add(implFile);
        });
    }

    private String getInterfaceContent(String name) {
        return StringHelper.of("public interface {name}Service {\n" +
                "    \n" +
                "}\n")
                .param("name", name)
                .toString();
    }

    private String getImplContent(String name) {
        return StringHelper.of("import lombok.extern.slf4j.Slf4j;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "@Slf4j\n" +
                "@Service\n" +
                "public class {name}ServiceImpl implements {name}Service {\n" +
                "    \n" +
                "}")
                .param("name", name)
                .toString();
    }
}
