package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.constant.AnnotationNames;
import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.context.ControllerContext;
import com.ganguo.java.plugin.context.HttpMethod;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.IndexUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 生成Api接口方法测试类
 */
@Slf4j
@ImportFrom({JavaFileContext.class, ControllerContext.class})
public class GenerateApiTestClassAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return ActionShowHelper.of(e)
                .isControllerApiMethod()
                .and(() -> Optional.ofNullable(ContextBuilder.of(this)
                        .put("event", e)
                        .build()
                        .get("apiTestFileExists", Boolean.class))
                        .map(b -> !b)
                        .orElse(false))
                .isShow();
    }

    @Func
    protected void doAction(Project project, VirtualFile apiTestDirFile, PsiDirectory apiTestDir,
                            boolean apiTestFileExists, @Nla VirtualFile apiTestFile,
                            FuncAction<PsiFile> createTestFile) {
        if (apiTestFileExists) {
            FileUtils.navigateFile(project, apiTestFile);
            return;
        }

        PsiFile newFile = createTestFile.get();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            apiTestDir.add(newFile);
            FileUtils.navigateFile(project, apiTestDirFile, newFile.getName());
        });
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String packageName, String apiTestClassName, PsiMethod curMethod,
                                       String httpMethod, String url, Project project, List<String> pathVars,
                                       Boolean hasReturn, Boolean isAdmin) {
        Map<String, Object> params = new HashMap<>(8);

        params.put("packageName", packageName);
        params.put("className", apiTestClassName);
        params.put("method", httpMethod);
        params.put("url", url);
        params.put("pathVars", pathVars);
        params.put("hasReturn", hasReturn);
        params.put("isAdmin", isAdmin);

        RequestBodyClass requestBodyClassName = getClassNameWithRequestBody(curMethod);
        if (requestBodyClassName != null) {
            params.put("requestClassName", requestBodyClassName.getName());
            params.put("requestClassSimpleName", requestBodyClassName.getSimpleName());
            params.put("requestSetters", getSetters(project, requestBodyClassName.getName()));
        }

        if (HttpMethod.METHOD_GET.equals(httpMethod)) {
            params.put("params", getQueryParams(curMethod));
        }

        return params;
    }

    /**
     * 创建测试类文件
     */
    @Func
    private PsiFile createTestFile(String apiTestClassName, FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.API_TEST_CLASS, apiTestClassName);
    }

    /**
     * 是否有返回值
     */
    @Var
    private Boolean hasReturn(PsiMethod curMethod) {
        return Optional.ofNullable(curMethod.getReturnType())
                .map(type -> !type.getPresentableText().equals("void"))
                .orElse(false);
    }

    /**
     * 是否是管理员模块
     */
    @Var
    private Boolean isAdmin(String baseUrl) {
        return baseUrl.contains("admin");
    }

    /**
     * Path参数列表
     */
    @Var
    private List<String> pathVars(PsiMethod curMethod) {
        return Arrays.stream(curMethod.getParameterList().getParameters())
                .filter(parameter -> parameter.getAnnotation(
                        AnnotationNames.PATH_VARIABLE) != null)
                .map(PsiNamedElement::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取RequestBody的Setter方法列表
     */
    private List<String> getSetters(Project project, String requestClassName) {
        return Optional.ofNullable(IndexUtils.getClassByQualifiedName(project, requestClassName))
                .map(psiClass -> PsiUtils.getAllSetter(psiClass)
                        .map(PsiMethod::getName)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * 获取标注有@RequestBody的参数的类名
     */
    private RequestBodyClass getClassNameWithRequestBody(PsiMethod method) {
        return Arrays.stream(method.getParameterList().getParameters())
                .filter(parameter -> Arrays.stream(parameter.getAnnotations())
                        .anyMatch(anno -> Optional.ofNullable(anno.getQualifiedName())
                                .map(name -> name.endsWith("RequestBody"))
                                .orElse(false)))
                .findFirst()
                .map(PsiParameter::getType)
                .map(type -> new RequestBodyClass(type.getCanonicalText(), type.getPresentableText()))
                .orElse(null);
    }

    /**
     * 获取GET方法的参数列表
     */
    private List<String> getQueryParams(PsiMethod method) {
        PsiParameter[] parameters = method.getParameterList().getParameters();
        List<String> params = new ArrayList<>(parameters.length);
        Arrays.stream(parameters)
                .filter(parameter -> {
                    String simpleName = parameter.getType().getPresentableText();
                    return !"HttpSession".equals(simpleName) &&
                            !"HttpServletRequest".equals(simpleName) &&
                            !"HttpServletResponse".equals(simpleName);
                })
                .filter(parameter -> parameter.getAnnotation(AnnotationNames.PATH_VARIABLE) == null)
                .forEach(parameter -> {
                    if ("Pageable".equals(parameter.getType().getPresentableText())) {
                        params.add("page");
                        params.add("size");
                    } else {
                        params.add(parameter.getName());
                    }
                });
        return params;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private static class RequestBodyClass {
        private final String name;
        private final String simpleName;
    }
}
