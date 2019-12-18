package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字段排序
 */
public class SortFieldAction extends BaseSortAction {

    @Func
    private void doAction(PsiClass selectedClass, FuncAction<Void> sortEnum, FuncAction<Void> sortClass) {
        if (selectedClass.isEnum()) {
            sortEnum.exec();
        } else {
            sortClass.exec();
        }
    }

    /**
     * 枚举类排序枚举值
     */
    @Func
    private void sortEnum(Project project, PsiClass selectedClass, WriteActions writeActions, PsiElementFactory elementFactory) {
        Collection<PsiEnumConstant> originalConstants = PsiTreeUtil.findChildrenOfType(
                selectedClass, PsiEnumConstant.class);
        List<PsiEnumConstant> sortedConstants = originalConstants.stream()
                .sorted(Comparator.comparing(this::getEnumGroupName)
                        .thenComparing(PsiField::getName))
                .map(constant -> (PsiEnumConstant) constant.copy())
                .collect(Collectors.toList());
        PsiComment comment = elementFactory.createCommentFromText("/**/", null);
        PsiWhiteSpace whiteSpace = PsiUtils.createWhiteSpace(project);
        PsiWhiteSpace newLine = PsiUtils.createWhiteSpace(project, 1);

        writeActions.add(() -> {
            String lastGroupName = null;
            boolean isFirst = true;
            Iterator<PsiEnumConstant> it = sortedConstants.iterator();

            for (PsiEnumConstant originalConstant : originalConstants) {
                PsiEnumConstant curConstant = (PsiEnumConstant) originalConstant.replace(it.next());

                // 删除原换行
                PsiElement prevElement = curConstant.getPrevSibling();
                if (prevElement instanceof PsiWhiteSpace && !prevElement.getText().equals("\n")) {
                    prevElement.replace(newLine.copy());
                }

                // 分组换行
                String groupName = getEnumGroupName(curConstant);
                if (!Objects.equals(groupName, lastGroupName)) {
                    if (!isFirst) {
                        curConstant.addBefore(comment.copy(), curConstant.getFirstChild())
                                .replace(whiteSpace.copy());
                    }

                    lastGroupName = groupName;
                    isFirst = false;
                }
            }
        }).run();
    }

    /**
     * 普通类排序字段
     */
    @Func
    private void sortClass(Project project, PsiClass selectedClass, WriteActions writeActions) {
        PsiElement whiteSpace = PsiUtils.createWhiteSpace(project);
        PsiElement locateElement = selectedClass.getLBrace();

        Arrays.stream(selectedClass.getFields())
                .sorted(Comparator.comparing(this::getFieldOrder).reversed()
                        .thenComparing(PsiField::getName).reversed())
                .filter(PsiElement::isPhysical)
                .forEachOrdered(field -> writeActions.add(() -> {
                    selectedClass.addAfter(field.copy(), locateElement);
                    selectedClass.addAfter(whiteSpace.copy(), locateElement);
                    field.delete();
                }));
        writeActions.run();
    }

    private String getEnumGroupName(PsiEnumConstant constant) {
        String name = constant.getName();
        int i = name.indexOf("__");
        return i == -1 ? name : name.substring(0, i);
    }

    private int getFieldOrder(PsiField field) {
        return getOrder(field.getModifierList());
    }
}
