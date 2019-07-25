package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.context.JavaFileContext;
import com.ganguo.plugin.util.StringHelper;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GenerateRepositoryMethodAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .importAll(JavaFileContext.getContext())
                .build()
                .execVoid("doAction");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return isMethodOfClass(e, "^I.*Repository.java$");
    }

    @Func
    private void doAction(AnActionEvent event, Project project, PsiMethod curMethod,
                          PsiClass daoClass, PsiClass implClass,
                          FuncAction<PsiMethod> createMethodForDao,
                          FuncAction<PsiMethod> createMethodForImpl,
                          FuncAction<Editor> getDaoEditor) {
        if (daoClass.findMethodBySignature(curMethod, false) == null) {
            createMethodForDao.exec().getOptional()
                    .ifPresent(method -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        daoClass.add(method);
                    }));
        }

        if (implClass.findMethodBySignature(curMethod, false) == null) {
            createMethodForImpl.exec().getOptional()
                    .ifPresent(method -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        implClass.add(method);
                    }));
        }

        // 跳转到DAO对应的方法处
        WriteCommandAction.runWriteCommandAction(project, () -> {
            new OpenFileDescriptor(project, daoClass.getContainingFile().getVirtualFile())
                    .navigateInEditor(project, true);

            getDaoEditor.exec().getOptional()
                    .ifPresent(editor -> Optional
                            .ofNullable(daoClass.findMethodBySignature(curMethod, false))
                            .map(PsiElement::getTextOffset)
                            .ifPresent(offset -> {
                                editor.getCaretModel().moveToOffset(offset);
                                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                            }));
        });
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
                                          FuncAction<PsiMethod> createMethodForDao, String moduleName) {
        PsiMethod method = createMethodForDao.get();

        PsiCodeBlock codeBlock = PsiTreeUtil.findChildOfType(method, PsiCodeBlock.class);
        if (codeBlock != null) {
            String[] parameters = Arrays.stream(method.getParameterList().getParameters())
                    .map(PsiNamedElement::getName)
                    .toArray(String[]::new);

            String newCodeBlockText = StringHelper.of("{" +
                    "return m{moduleName}DbStrategy.{methodName}({parameters});" +
                    "}")
                    .param("moduleName", moduleName)
                    .param("methodName", method.getName())
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
}
