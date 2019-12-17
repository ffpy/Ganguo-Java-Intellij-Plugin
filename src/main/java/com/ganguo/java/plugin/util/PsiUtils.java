package com.ganguo.java.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class PsiUtils {

    /**
     * 获取文件的第一个类
     *
     * @param file Java文件
     * @return PsiClass
     */
    public static PsiClass getClassByFile(PsiJavaFile file) {
        PsiClass[] classes = file.getClasses();
        if (classes.length > 0) {
            return classes[0];
        }
        return null;
    }

    /**
     * 获取元素所属的类
     *
     * @param element PsiElement
     * @return PsiClass
     */
    public static PsiClass getClassByElement(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    /**
     * 格式化代码
     */
    public static void reformatJavaFile(PsiElement theElement) {
        if (theElement == null) {
            return;
        }
        WriteCommandAction.runWriteCommandAction(theElement.getProject(), () -> {
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(theElement.getProject());
            try {
                codeStyleManager.reformat(theElement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 创建文档注释
     *
     * @param comment 注释文本
     * @return PsiComment
     */
    public static PsiComment createPsiDocComment(PsiElementFactory factory, String comment) {
        return createPsiDocComment(factory, comment, true);
    }

    /**
     * 创建文档注释
     *
     * @param comment   注释文本
     * @param multiLine 是否为多行注释
     * @return PsiComment
     */
    public static PsiComment createPsiDocComment(
            PsiElementFactory factory, String comment, boolean multiLine) {
        StringBuilder commentText = new StringBuilder(comment.length());

        if (multiLine) {
            commentText.append("/**\n");

            int fromIndex = 0;
            int index;
            do {
                index = comment.indexOf('\n', fromIndex);
                if (index == -1) {
                    index = comment.length();
                }

                commentText.append("* ").append(comment, fromIndex, index).append("\n");
                fromIndex = index + 1;
            } while (index != comment.length());

            commentText.append("*/");
        } else {
            commentText.append("/** ").append(comment).append(" */");
        }

        return factory.createDocCommentFromText(commentText.toString(), null);
    }

    public static <T> T getAnnotationValue(PsiAnnotation annotation, String name, Class<T> type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);

        return Optional.ofNullable(annotation)
                .map(anno -> anno.findAttributeValue(name))
                .map(PsiElement::getText)
                .map(text -> {
                    // 字符串类型的会有双引号包围，要去掉，如value = "xiaoming"，则text="\"xiaoming\""
                    if (type == String.class && text.length() >= 2) {
                        text = text.substring(1, text.length() - 1);
                    }
                    //noinspection unchecked
                    return (T) ConvertUtils.convert(text, type);
                })
                .orElse(null);

    }

    public static Stream<PsiMethod> getAllSetter(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllMethods())
                .filter(method -> {
                    PsiModifierList modifierList = method.getModifierList();
                    return modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                            !modifierList.hasModifierProperty(PsiModifier.STATIC);
                })
                .filter(method -> method.getName().matches("^set[A-Z].*$"))
                .filter(method -> method.getParameterList().getParametersCount() == 1);
    }

    public static Stream<String> getAllSetterName(PsiClass psiClass) {
        return getAllSetter(psiClass)
                .map(method -> {
                    String name = method.getName();
                    if (name.startsWith("set")) {
                        return StringUtils.uncapitalize(name.substring("set".length()));
                    } else {
                        return name;
                    }
                });
    }

    public static Stream<PsiMethod> getAllGetter(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllMethods())
                .filter(method -> {
                    PsiModifierList modifierList = method.getModifierList();
                    return modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                            !modifierList.hasModifierProperty(PsiModifier.STATIC);
                })
                .filter(method -> method.getName().matches("^get[A-Z].*$") ||
                        method.getName().matches("^is[A-Z].*$"))
                .filter(method -> method.getParameterList().getParametersCount() == 0)
                .filter(method -> method.getReturnType() != null &&
                        !method.getReturnType().getPresentableText().equals("void"));
    }

    public static Stream<String> getAllGetterName(PsiClass psiClass) {
        return getAllGetter(psiClass)
                .map(method -> {
                    String name = method.getName();
                    if (name.startsWith("is")) {
                        return StringUtils.uncapitalize(name.substring("is".length()));
                    } else if (name.startsWith("get")) {
                        return StringUtils.uncapitalize(name.substring("get".length()));
                    } else {
                        return name;
                    }
                });
    }

    /**
     * 创建空行
     */
    public static PsiWhiteSpace createWhiteSpace(Project project) {
        return (PsiWhiteSpace) PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n\n");
    }

    /**
     * 创建换行
     */
    public static PsiWhiteSpace createWhiteSpace(Project project, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater than zero.");
        }
        return (PsiWhiteSpace) PsiParserFacade.SERVICE.getInstance(project)
                .createWhiteSpaceFromText(StringUtils.repeat("\n", n));
    }
}
