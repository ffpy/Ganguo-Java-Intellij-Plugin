package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.DefaultValue;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Ignore;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@ImportFrom(JavaFileContext.class)
public class ControllerContext {

    /**
     * 接口的Http方法
     */
    @Var
    public String httpMethod(PsiMethod curMethod) {
        for (PsiAnnotation annotation : curMethod.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;

            if (qualifiedName.endsWith("GetMapping")) {
                return HttpMethod.METHOD_GET;
            } else if (qualifiedName.endsWith("PostMapping")) {
                return HttpMethod.METHOD_POST;
            } else if (qualifiedName.endsWith("PutMapping")) {
                return HttpMethod.METHOD_PUT;
            } else if (qualifiedName.endsWith("DeleteMapping")) {
                return HttpMethod.METHOD_DELETE;
            }
        }
        return null;
    }

    /**
     * 接口URL
     */
    @Var
    public String url(@DefaultValue("") String baseUrl, @DefaultValue("") String pathUrl) {
        return (baseUrl + pathUrl).replace("//", "/");
    }

    /**
     * Controller的路径
     */
    @Var
    public String baseUrl(PsiClass curClass) {
        return Arrays.stream(curClass.getAnnotations())
                .filter(anno -> Optional.ofNullable(anno.getQualifiedName())
                        .map(name -> name.endsWith("RequestMapping"))
                        .orElse(false))
                .findFirst()
                .map(anno -> PsiUtils.getAnnotationValue(anno, "value", String.class))
                .orElse(null);
    }

    /**
     * 方法上的路径
     */
    @Var
    public String pathUrl(PsiAnnotation curMapping) {
        return PsiUtils.getAnnotationValue(curMapping, "value", String.class);
    }

    /**
     * 方法上的XXXMapping注解
     */
    @Var
    public PsiAnnotation curMapping(PsiMethod curMethod) {
        return Arrays.stream(curMethod.getAnnotations())
                .filter(anno -> {
                    String name = anno.getQualifiedName();
                    if (name == null) {
                        return false;
                    }
                    return name.endsWith("GetMapping") ||
                            name.endsWith("PostMapping") ||
                            name.endsWith("PutMapping") ||
                            name.endsWith("DeleteMapping") ||
                            name.endsWith("RequestMapping");
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 模块名，如api/user
     */
    @Var
    public String moduleName(String curPackageName) {
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
    public VirtualFile apiTestDirFile(String moduleName, VirtualFile testPackageFile) {
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
    public PsiDirectory apiTestDir(PsiDirectoryFactory directoryFactory, VirtualFile apiTestDirFile) {
        return directoryFactory.createDirectory(apiTestDirFile);
    }

    /**
     * 测试类类名
     */
    @Var
    public String apiTestClassName(PsiMethod curMethod, FuncAction<String> getTestClassNamByMethodName) {
        return getTestClassNamByMethodName.exec(curMethod.getName()).get();
    }

    /**
     * 从接口方法名获取测试类类名
     */
    @Func
    public String getTestClassNamByMethodName(@Ignore String methodName) {
        return StringUtils.capitalize(methodName) + "Tests";
    }

    /**
     * 测试类文件
     */
    @Var
    public VirtualFile apiTestFile(VirtualFile apiTestDirFile, String apiTestClassName) {
        return apiTestDirFile.findFileByRelativePath(apiTestClassName + ".java");
    }

    /**
     * 测试类是否已存在
     *
     * @return true为存在，false为不存在
     */
    @Var
    public boolean apiTestFileExists(@Nla VirtualFile apiTestFile) {
        return Optional.ofNullable(apiTestFile)
                .map(VirtualFile::exists)
                .orElse(false);
    }
}
