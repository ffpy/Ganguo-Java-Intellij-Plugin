package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.StringHelper;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ImportFrom(JavaFileContext.class)
public class GenerateRepositoryMethodAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return isMethodOfClass(e, "^I.*Repository.java$");
    }

    @Func
    private void doAction(Project project, PsiMethod curMethod, PsiClass daoClass, PsiClass implClass,
                          FuncAction<PsiMethod> createMethodForDao, FuncAction<PsiMethod> createMethodForImpl,
                          FuncAction<Editor> getDaoEditor, WriteActions writeActions,
                          @Nla PsiMethod daoPrevMethod, @Nla PsiMethod implPrevMethod,
                          @Nla PsiMethod daoNextMethod, @Nla PsiMethod implNextMethod) {
        if (daoClass.findMethodBySignature(curMethod, false) == null) {
            createMethodForDao.exec().ifPresent(method -> writeActions.add(() ->
                    addMethod2Class(daoClass, method, daoPrevMethod, daoNextMethod)));
        }

        if (implClass.findMethodBySignature(curMethod, false) == null) {
            createMethodForImpl.exec().ifPresent(method -> writeActions.add(() ->
                    addMethod2Class(implClass, method, implPrevMethod, implNextMethod)));
        }

        // 跳转到DAO对应的方法处
        writeActions.add(() -> {
            new OpenFileDescriptor(project, daoClass.getContainingFile().getVirtualFile())
                    .navigateInEditor(project, true);

            getDaoEditor.exec().ifPresent(editor -> Optional
                    .ofNullable(daoClass.findMethodBySignature(curMethod, false))
                    .map(PsiElement::getTextOffset)
                    .ifPresent(offset -> {
                        editor.getCaretModel().moveToOffset(offset);
                        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    }));
        }).run();
    }

    @Var
    private PsiMethod prevMethod(PsiMethod curMethod) {
        return PsiTreeUtil.getPrevSiblingOfType(curMethod, PsiMethod.class);
    }

    @Var
    private PsiMethod daoPrevMethod(@Nla PsiMethod prevMethod, PsiClass daoClass) {
        return Optional.ofNullable(prevMethod)
                .map(method -> daoClass.findMethodBySignature(method, false))
                .orElse(null);
    }

    @Var
    private PsiMethod implPrevMethod(@Nla PsiMethod prevMethod, PsiClass implClass) {
        return Optional.ofNullable(prevMethod)
                .map(method -> implClass.findMethodBySignature(method, false))
                .orElse(null);
    }

    @Var
    private PsiMethod nextMethod(PsiMethod curMethod) {
        return PsiTreeUtil.getNextSiblingOfType(curMethod, PsiMethod.class);
    }

    @Var
    private PsiMethod daoNextMethod(@Nla PsiMethod nextMethod, PsiClass daoClass) {
        return Optional.ofNullable(nextMethod)
                .map(method -> daoClass.findMethodBySignature(method, false))
                .orElse(null);
    }

    @Var
    private PsiMethod implNextMethod(@Nla PsiMethod nextMethod, PsiClass implClass) {
        return Optional.ofNullable(nextMethod)
                .map(method -> implClass.findMethodBySignature(method, false))
                .orElse(null);
    }

    @Func
    private Editor getDaoEditor(PsiClass daoClass) {
        Editor[] editors = EditorFactory.getInstance().getAllEditors();
        for (int i = editors.length - 1; i >= 0; i--) {
            Editor editor = editors[i];
            if (editor.getDocument().getText().contains("public class " + daoClass.getName())) {
                return editor;
            }
        }
        return null;
    }

    @Func
    private PsiMethod createMethodForDao(Project project, PsiMethod curMethod, PsiElementFactory elementFactory) {
        PsiMethod method = (PsiMethod) curMethod.copy();

        PsiDocComment docComment = PsiTreeUtil.findChildOfType(method, PsiDocComment.class);
        PsiElement semicolon = method.getLastChild();

        PsiCodeBlock codeBlock = elementFactory.createCodeBlock();
        PsiKeyword publicKeyword = elementFactory.createKeyword("public");
        PsiAnnotation override = elementFactory.createAnnotationFromText("@Override", method);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            Optional.ofNullable(docComment).ifPresent(PsiElement::delete);
            Optional.ofNullable(semicolon).ifPresent(PsiElement::delete);
            method.addBefore(publicKeyword, method.getFirstChild());
            method.addBefore(override, method.getFirstChild());
            method.add(codeBlock);
        });

        return method;
    }

    @Func
    private PsiMethod createMethodForImpl(Project project, PsiElementFactory elementFactory,
                                          FuncAction<PsiMethod> createMethodForDao, String moduleName,
                                          PsiClass implClass) {
        PsiMethod method = createMethodForDao.get();

        PsiCodeBlock codeBlock = PsiTreeUtil.findChildOfType(method, PsiCodeBlock.class);
        if (codeBlock != null) {
            String[] parameters = Arrays.stream(method.getParameterList().getParameters())
                    .map(PsiNamedElement::getName)
                    .toArray(String[]::new);

            String dbStrategyField = Arrays.stream(implClass.getAllFields())
                    .filter(field -> field.getType().getPresentableText()
                            .equalsIgnoreCase("I" + moduleName + "DbStrategy"))
                    .findFirst()
                    .map(NavigationItem::getName)
                    .orElse("m" + moduleName + "DbStrategy");

            String newCodeBlockText = StringHelper.of("{" +
                    "return {dbStrategyField}.{methodName}({parameters});" +
                    "}")
                    .param("methodName", method.getName())
                    .param("dbStrategyField", dbStrategyField)
                    .param("parameters", StringUtils.join(parameters, ", "))
                    .toString();

            PsiCodeBlock newCodeBlock = elementFactory.createCodeBlockFromText(newCodeBlockText, method);

            WriteCommandAction.runWriteCommandAction(project, () -> {
                codeBlock.replace(newCodeBlock);
            });
        }

        return method;
    }

    @Var
    private String moduleName(PsiJavaFile curFile) {
        Matcher matcher = Pattern.compile("I(.*)Repository.java").matcher(curFile.getName());
        return matcher.find() ? StringUtils.capitalize(matcher.group(1)) : null;
    }

    @Var
    private PsiJavaFile daoFile(String moduleName, FuncAction<PsiFile> getFilesByName) {
        PsiFile file = getFilesByName.get(moduleName + "DAO.java");
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    @Var
    private PsiJavaFile implFile(String moduleName, FuncAction<PsiFile> getFilesByName) {
        PsiFile file = getFilesByName.get(moduleName + "Repository.java");
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    @Var
    private PsiClass daoClass(PsiJavaFile daoFile) {
        return Arrays.stream(daoFile.getClasses()).findFirst().orElse(null);
    }

    @Var
    private PsiClass implClass(PsiJavaFile implFile) {
        return Arrays.stream(implFile.getClasses()).findFirst().orElse(null);
    }

    /**
     * 添加方法到类的指定位置
     *
     * @param psiClass   类
     * @param method     要添加的方法
     * @param prevMethod 前一个位置方法
     * @param nextMethod 下一个位置方法
     */
    private void addMethod2Class(PsiClass psiClass, PsiMethod method,
                                 @Nullable PsiMethod prevMethod, @Nullable PsiMethod nextMethod) {
        if (prevMethod != null) {
            psiClass.addAfter(method, prevMethod);
        } else if (nextMethod != null) {
            psiClass.addBefore(method, nextMethod);
        } else {
            psiClass.add(method);
        }
    }
}
