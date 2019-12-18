package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.util.MsgUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.dependcode.dependcode.anno.Func;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 方法排序
 */
public class SortMethodAction extends BaseSortAction {

    @Func
    private void doAction(Project project, PsiClass selectedClass, WriteActions writeActions) {
        sortClasses(project, Collections.singletonList(selectedClass), writeActions);
        writeActions.run();
    }

    protected void sortClasses(Project project, List<PsiClass> psiClasses, WriteActions writeActions) {
        PsiElement whiteSpace = PsiUtils.createWhiteSpace(project);

        for (PsiClass psiClass : psiClasses) {
            PsiElement locateElement = Arrays.stream(psiClass.getFields())
                    .filter(PsiElement::isPhysical)
                    .reduce((field1, field2) -> field2)
                    .map(field -> (PsiElement) field)
                    .orElseGet(psiClass::getLBrace);

            Arrays.stream(psiClass.getMethods())
                    .filter(PsiElement::isPhysical)
                    .sorted(getComparator())
                    .forEachOrdered(method -> writeActions.add(() -> {
                        PsiElement e = psiClass.addAfter(method.copy(), locateElement);
                        psiClass.addBefore(whiteSpace, e);
                        method.delete();
                    }));
        }
    }

    protected Comparator<PsiMethod> getComparator() {
        return Comparator.comparing(this::getMethodOrder).reversed()
                .thenComparing(PsiMethod::getName).reversed();
    }

    protected int getMethodOrder(PsiMethod method) {
        int order = getOrder(method.getModifierList());
        if (method.hasAnnotation("org.springframework.scheduling.annotation.Scheduled")) {
            order += 50;
        }
        return order;
    }
}
