package com.ganguo.java.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.beanutils.ConvertUtils;

import java.util.Objects;
import java.util.Optional;

public class PsiUtils {

    public static PsiClass getClassByFile(PsiJavaFile file) {
        PsiClass[] classes = file.getClasses();
        if (classes.length > 0) {
            return classes[0];
        }
        return null;
    }

    /**
     * 格式化代码
     */
    public static void reformatJavaFile(PsiElement theElement) {
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
}
