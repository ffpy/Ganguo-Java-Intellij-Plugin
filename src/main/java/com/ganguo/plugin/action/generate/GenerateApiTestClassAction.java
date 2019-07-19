package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.action.BaseAction;
import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.util.ElementUtils;
import com.ganguo.plugin.util.FileUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.CodeContextBuilder;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 生成Api接口方法测试类
 */
@Slf4j
public class GenerateApiTestClassAction extends BaseAction {

    @Override
    protected void action(AnActionEvent e) {
        CodeContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Func
    protected void doAction(Context context, Project project,
                            VirtualFile testDirFile, PsiDirectory testDir) {
        if (context.exec("checkTargetFileExists", Boolean.class).get()) {
            return;
        }

        PsiFile newFile = context.exec("createNewFile", PsiFile.class).get();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            testDir.add(newFile);
            FileUtils.navigateFile(project, testDirFile, newFile.getName());
        });
    }

    @Var
    private PsiJavaFile curFile(AnActionEvent event) {
        return (PsiJavaFile) event.getData(LangDataKeys.PSI_FILE);
    }

    @Var
    private PsiMethod curMethod(AnActionEvent event) {
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        if (!ElementUtils.isMethodElement(psiElement)) {
            return null;
        }
        return (PsiMethod) psiElement;
    }

    @Var
    private String curPackageName(PsiJavaFile curFile) {
        return curFile.getPackageName();
    }

    @Var
    private String moduleName(String curPackageName) {
        final String sep = ".controller";
        int index = curPackageName.indexOf(sep);
        String moduleName;
        if (index == -1) {
            moduleName = "";
        } else {
            moduleName = curPackageName.substring(index + sep.length())
                    .replace('.', '/');
            if (moduleName.startsWith("/")) {
                moduleName = moduleName.substring(1);
            }
        }
        return moduleName;
    }

    @Var
    private VirtualFile testDirFile(String moduleName, VirtualFile testPackageFile) {
        try {
            return FileUtils.findOrCreateDirectory(testPackageFile, "controller/" + moduleName);
        } catch (IOException ex) {
            log.error("create or get {} fail!", moduleName, ex);
        }
        return null;
    }

    @Var
    private PsiDirectory testDir(PsiDirectoryFactory directoryFactory, VirtualFile testDirFile) {
        return directoryFactory.createDirectory(testDirFile);
    }

    @Var
    private String className(PsiMethod curMethod) {
        return StringUtils.capitalize(curMethod.getName());
    }

    @Var
    private Map<String, String> params(String packageName, String className, PsiMethod curMethod,
                                       PsiJavaFile curFile) {
        Map<String, String> params = new HashMap<>(8);

        params.put("packageName", packageName);
        params.put("className", className);
        params.put("method", getHttpMethod(curMethod));
        params.put("url", getUrl(curFile, curMethod));

        RequestBodyClass requestBodyClassName = getClassNameWithRequestBody(curMethod);
        if (requestBodyClassName != null) {
            params.put("requestClassName", requestBodyClassName.getName());
            params.put("requestClassSimpleName", requestBodyClassName.getSimpleName());
        }

        return params;
    }

    @Var
    private String targetFilename(String className) {
        return className + "Tests";
    }

    @Func
    private PsiFile createNewFile(Context context, String targetFilename) {
        return context.exec("createJavaFile", PsiFile.class,
                TemplateName.API_TEST_CLASS, targetFilename).get();
    }

    @Var
    private VirtualFile targetFile(VirtualFile testDirFile, String targetFilename) {
        return testDirFile.findFileByRelativePath(targetFilename + ".java");
    }

    @Func
    private boolean checkTargetFileExists(Project project, @Nla VirtualFile targetFile) {
        boolean exists = Optional.ofNullable(targetFile)
                .map(VirtualFile::exists)
                .orElse(false);
        // 文件已存在则跳转
        if (exists) {
            FileUtils.navigateFile(project, targetFile);
        }
        return exists;
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

    private String getHttpMethod(PsiMethod psiMethod) {
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

    @Getter
    @AllArgsConstructor
    @ToString
    private class RequestBodyClass {
        private final String name;
        private final String simpleName;
    }
}
