package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.context.JavaFileContext;
import com.ganguo.plugin.util.FileUtils;
import com.ganguo.plugin.util.PsiUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiDirectory;
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
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 生成Api接口方法测试类
 */
@Slf4j
public class GenerateApiTestClassAction extends BaseGenerateAction {

    private static final String METHOD_GET = "get";
    private static final String METHOD_POST = "post";
    private static final String METHOD_PUT = "put";
    private static final String METHOD_DELETE = "delete";

    @Override
    protected void action(AnActionEvent e) {
        ContextBuilder.of(this)
                .put("event", e)
                .importAll(JavaFileContext.getContext())
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return isMethodOfClass(e, "^.*Controller.java$");
    }

    @Func
    protected void doAction(Project project, VirtualFile testDirFile, PsiDirectory testDir,
                            FuncAction<Boolean> checkTargetFileExists, FuncAction<PsiFile> createNewFile) {
        if (checkTargetFileExists.get()) {
            return;
        }

        PsiFile newFile = createNewFile.get();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            testDir.add(newFile);
            FileUtils.navigateFile(project, testDirFile, newFile.getName());
        });
    }

    /**
     * 模块名
     */
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

    /**
     * 接口测试文件夹
     */
    @Var
    private VirtualFile testDirFile(String moduleName, VirtualFile testPackageFile) {
        try {
            return FileUtils.findOrCreateDirectory(testPackageFile, "controller/" + moduleName);
        } catch (IOException ex) {
            log.error("create or get {} fail!", moduleName, ex);
        }
        return null;
    }

    /**
     * 接口测试文件夹
     */
    @Var
    private PsiDirectory testDir(PsiDirectoryFactory directoryFactory, VirtualFile testDirFile) {
        return directoryFactory.createDirectory(testDirFile);
    }

    /**
     * 测试类类名
     */
    @Var
    private String className(PsiMethod curMethod) {
        return StringUtils.capitalize(curMethod.getName());
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String packageName, String className, PsiMethod curMethod,
                                       String httpMethod, String url) {
        Map<String, Object> params = new HashMap<>(8);

        params.put("packageName", packageName);
        params.put("className", className);
        params.put("method", httpMethod);
        params.put("url", url);

        RequestBodyClass requestBodyClassName = getClassNameWithRequestBody(curMethod);
        if (requestBodyClassName != null) {
            params.put("requestClassName", requestBodyClassName.getName());
            params.put("requestClassSimpleName", requestBodyClassName.getSimpleName());
        }

        if (METHOD_GET.equals(httpMethod)) {
            params.put("params", getQueryParams(curMethod));
        }

        return params;
    }

    /**
     * 测试类名
     */
    @Var
    private String targetFilename(String className) {
        return className + "Tests";
    }

    /**
     * 创建测试类文件
     */
    @Func
    private PsiFile createNewFile(String targetFilename, FuncAction<PsiFile> createJavaFile) {
        return createJavaFile.get(TemplateName.API_TEST_CLASS, targetFilename);
    }

    /**
     * 已存在的测试类文件
     */
    @Var
    private VirtualFile targetFile(VirtualFile testDirFile, String targetFilename) {
        return testDirFile.findFileByRelativePath(targetFilename + ".java");
    }

    /**
     * 接口的Http方法
     */
    @Var
    private String httpMethod(PsiMethod curMethod) {
        for (PsiAnnotation annotation : curMethod.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;

            if (qualifiedName.endsWith("GetMapping")) {
                return METHOD_GET;
            } else if (qualifiedName.endsWith("PostMapping")) {
                return METHOD_POST;
            } else if (qualifiedName.endsWith("PutMapping")) {
                return METHOD_PUT;
            } else if (qualifiedName.endsWith("DeleteMapping")) {
                return METHOD_DELETE;
            }
        }
        return null;
    }

    /**
     * 接口URL
     */
    @Var
    private String url(PsiJavaFile curFile, PsiMethod curMethod) {
        String baseUrl = Arrays.stream(curFile.getClasses())
                .findFirst()
                .flatMap(cls -> Arrays.stream(cls.getAnnotations())
                        .filter(anno -> Optional.ofNullable(anno.getQualifiedName())
                                .map(name -> name.endsWith("RequestMapping"))
                                .orElse(false))
                        .findFirst())
                .map(anno -> PsiUtils.getAnnotationValue(anno, "value", String.class))
                .orElse("");

        String subUrl = Arrays.stream(curMethod.getAnnotations())
                .filter(anno -> Optional.ofNullable(anno.getQualifiedName())
                        .map(name -> name.endsWith("GetMapping") ||
                                name.endsWith("PostMapping") ||
                                name.endsWith("PutMapping") ||
                                name.endsWith("DeleteMapping"))
                        .orElse(false))
                .findFirst()
                .map(anno -> PsiUtils.getAnnotationValue(anno, "value", String.class))
                .orElse("");


        return (baseUrl + subUrl).replace("//", "/");
    }

    /**
     * 检查测试类是否已存在，如果存在则跳转
     *
     * @return true为存在，false为不存在
     */
    @Func
    private boolean checkTargetFileExists(Project project, @Nla VirtualFile targetFile) {
        boolean exists = Optional.ofNullable(targetFile)
                .map(VirtualFile::exists)
                .orElse(false);
        if (exists) {
            FileUtils.navigateFile(project, targetFile);
        }
        return exists;
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
                .filter(param -> {
                    String simpleName = param.getType().getPresentableText();
                    return !"HttpSession".equals(simpleName) &&
                            !"HttpServletRequest".equals(simpleName) &&
                            !"HttpServletResponse".equals(simpleName);
                })
                .forEach(param -> {
                    if ("Pageable".equals(param.getType().getPresentableText())) {
                        params.add("page");
                        params.add("size");
                    } else {
                        params.add(param.getName());
                    }
                });
        return params;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private class RequestBodyClass {
        private final String name;
        private final String simpleName;
    }
}
