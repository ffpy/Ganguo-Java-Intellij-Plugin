package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Var;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从测试类跳转到接口方法
 */
@Slf4j
@ImportFrom(JavaFileContext.class)
public class GotoApiMethodAction extends BaseGenerateAction {
    private static final String CLASS_NAME_SUFFIX = "Tests";
    private static final Pattern MODULE_PATH_PATTERN =
            Pattern.compile("/src/test/java/com/ganguomob/dev/.*/controller/(.*)/[\\w]*.java");

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return ActionShowHelper.of(e)
                .elementType(PsiClass.class)
                .filePathMatch(".*/src/test/java/com/ganguomob/dev/.*/controller/.*Tests\\.java")
                .isShow();
    }

    @Var
    private PsiClass curClass(PsiElement curElement) {
        return curElement instanceof PsiClass ? (PsiClass) curElement : null;
    }

    @Var
    private String apiMethodName(PsiClass curClass) {
        return Optional.of(curClass)
                .map(PsiNamedElement::getName)
                .filter(className -> className.endsWith(CLASS_NAME_SUFFIX))
                .map(className -> className.substring(0, className.length() - CLASS_NAME_SUFFIX.length()))
                .map(StringUtils::uncapitalize)
                .orElse(null);
    }

    @Var
    private String module(PsiJavaFile curFile) {
        Matcher matcher = MODULE_PATH_PATTERN.matcher(curFile.getVirtualFile().getPath());
        return matcher.find() ? matcher.group(1) : null;
    }

    @Var
    private VirtualFile controllerDirFile(VirtualFile packageFile) {
        return packageFile.findFileByRelativePath("/ui/controller");
    }

    @Var
    private VirtualFile moduleDirFile(String module, VirtualFile controllerDirFile) {
        return controllerDirFile.findFileByRelativePath(module);
    }

    @Func
    private void doAction(String apiMethodName, VirtualFile moduleDirFile, PsiManager psiManager, WriteActions writeActions, Project project) {
        Arrays.stream(moduleDirFile.getChildren())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().endsWith("Controller.java"))
                .map(psiManager::findFile)
                .filter(Objects::nonNull)
                .filter(file -> file instanceof PsiJavaFile)
                .map(file -> (PsiJavaFile) file)
                .map(PsiUtils::getClassByFile)
                .filter(Objects::nonNull)
                .forEach(psiClass -> showClass(apiMethodName, project, psiClass, writeActions));
    }

    private void showClass(String apiMethodName, Project project, PsiClass psiClass, WriteActions writeActions) {
        Arrays.stream(psiClass.findMethodsByName(apiMethodName, false))
                .findFirst()
                .ifPresent(method -> writeActions.add(() -> {
                    FileUtils.navigateFileInEditor(project, psiClass.getContainingFile().getVirtualFile());
                    Optional.ofNullable(EditorUtils.getEditorByClassName(psiClass.getName()))
                            .ifPresent(editor -> {
                                editor.getCaretModel().moveToOffset(method.getTextOffset());
                                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                            });
                }).run());
    }
}
