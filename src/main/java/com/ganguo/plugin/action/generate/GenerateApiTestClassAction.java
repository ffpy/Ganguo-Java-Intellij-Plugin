package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.service.ProjectSettingService;
import com.ganguo.plugin.util.ElementUtils;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.MsgUtils;
import com.ganguo.plugin.util.ProjectUtils;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GenerateApiTestClassAction extends BaseAction {

    @Override
    protected void action(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (!ElementUtils.isMethodElement(psiElement)) {
            return;
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;

        String methodName = psiMethod.getName();

        String className = StringUtils.capitalize(methodName);

        String packageName = ((PsiJavaFile) psiFile).getPackageName();

        final String sep = ".controller";
        int index = packageName.indexOf(sep);
        String moduleName;
        if (index == -1) {
            moduleName = "";
        } else {
            moduleName = packageName.substring(index + sep.length())
                    .replace('.', '/');
            if (moduleName.startsWith("/")) {
                moduleName = moduleName.substring(1);
            }
        }

        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        VirtualFile testDirFile;
        try {
            testDirFile = FileUtils.findOrCreateDirectory(
                    ProjectUtils.getTestPackageFile(project), "controller/" + moduleName);
        } catch (IOException ex) {
            ex.printStackTrace();
            MsgUtils.error("create or get %s fail!", moduleName);
            return;
        }

        PsiDirectory testDir = directoryFactory.createDirectory(testDirFile);

        String targetFilename = className + "Tests.java";
        VirtualFile targetFile = testDirFile.findFileByRelativePath(targetFilename);

        Boolean targetFileExists = Optional.ofNullable(targetFile)
                .map(VirtualFile::exists)
                .orElse(false);

        // 文件已存在则跳转
        if (targetFileExists) {
            new OpenFileDescriptor(project, targetFile).navigate(true);
            return;
        }

        RequestBodyClass requestBodyClassName = getClassNameWithRequestBody(psiMethod);

        Map<String, String> params = new HashMap<>();
        params.put("packageName", ProjectUtils.getPackageName(project));
        params.put("className", className);
        params.put("method", getMethod(psiMethod));
        params.put("url", getUrl(psiFile, psiMethod));
        if (requestBodyClassName != null) {
            params.put("requestClassName", requestBodyClassName.getName());
            params.put("requestClassSimpleName", requestBodyClassName.getSimpleName());
        }

        ProjectSettingService settingService =
                ServiceManager.getService(project, ProjectSettingService.class);

        PsiFile newFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE,
                TemplateUtils.fromString(settingService.getTemplate(TemplateName.API_TEST_CLASS),
                        params));
        newFile.setName(targetFilename);

        getClassNameWithRequestBody(psiMethod);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            testDir.add(newFile);
            FileUtils.navigateFile(project, testDirFile.findChild(targetFilename));
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = Optional.ofNullable(e.getData(LangDataKeys.PSI_FILE))
                .map(PsiFile::getVirtualFile)
                .map(VirtualFile::getPath)
                .map(path -> path.endsWith("Controller.java"))
                .filter(Boolean::booleanValue)
                .map(it -> e.getData(LangDataKeys.PSI_ELEMENT))
                .map(ElementUtils::isMethodElement)
                .orElse(false);

        e.getPresentation().setEnabled(enabled);
    }

    private String getMethod(PsiMethod psiMethod) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;

            if (qualifiedName.endsWith("GetMapping")) {
                return "get";
            } else if (qualifiedName.endsWith("PostMapping")) {
                return "post";
            } else if (qualifiedName.endsWith("PutMapping")) {
                return "put";
            } else if (qualifiedName.endsWith("DeleteMapping")) {
                return "delete";
            }
        }
        return null;
    }

    private String getUrl(PsiFile psiFile, PsiMethod psiMethod) {
        String baseUrl = Arrays.stream(((PsiJavaFile) psiFile).getClasses())
                .findFirst()
                .flatMap(cls -> Arrays.stream(cls.getAnnotations())
                        .filter(anno -> Optional.ofNullable(anno.getQualifiedName())
                                .map(name -> name.endsWith("RequestMapping"))
                                .orElse(false))
                        .findFirst())
                .map(anno -> anno.findAttributeValue("value"))
                .map(PsiElement::getText)
                .filter(StringUtils::isNotEmpty)
                .map(url -> url.substring(1, url.length() - 1))
                .orElse("");

        String subUrl = Arrays.stream(psiMethod.getAnnotations())
                .filter(anno -> Optional.ofNullable(anno.getQualifiedName())
                        .map(name -> name.endsWith("GetMapping") ||
                                name.endsWith("PostMapping") ||
                                name.endsWith("PutMapping") ||
                                name.endsWith("DeleteMapping"))
                        .orElse(false))
                .findFirst()
                .flatMap(anno -> Optional.ofNullable(anno.findAttributeValue("value"))
                        .map(PsiElement::getText)
                        .filter(StringUtils::isNotEmpty)
                        .map(url -> url.substring(1, url.length() - 1)))
                .orElse("");


        return (baseUrl + subUrl).replace("//", "/");
    }

    private RequestBodyClass getClassNameWithRequestBody(PsiMethod psiMethod) {
        return Arrays.stream(psiMethod.getParameterList().getParameters())
                .filter(parameter -> Arrays.stream(parameter.getAnnotations())
                        .anyMatch(anno -> Optional.ofNullable(anno.getQualifiedName())
                                .map(name -> name.endsWith("RequestBody"))
                                .orElse(false)))
                .findFirst()
                .map(PsiParameter::getType)
                .map(type -> new RequestBodyClass(type.getCanonicalText(), type.getPresentableText()))
                .orElse(null);
    }

    private class RequestBodyClass {
        private final String name;
        private final String simpleName;

        public RequestBodyClass(String name, String simpleName) {
            this.name = name;
            this.simpleName = simpleName;
        }

        public String getName() {
            return name;
        }

        public String getSimpleName() {
            return simpleName;
        }
    }
}
