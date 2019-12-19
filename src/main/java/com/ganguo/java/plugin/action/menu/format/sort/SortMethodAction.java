package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameterList;
import org.dependcode.dependcode.anno.Func;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 方法排序
 */
public class SortMethodAction extends BaseSortAction {

    public static final String SCHEDULED_ANNOTATION_NAME = "org.springframework.scheduling.annotation.Scheduled";

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
                    .sorted(getComparator()
                            .thenComparing(PsiMethod::getName)
                            .thenComparing(PsiMethod::getParameterList,
                                    Comparator.comparingInt(PsiParameterList::getParametersCount))
                            .reversed())
                    .forEachOrdered(method -> writeActions.add(() -> {
                        PsiElement e = psiClass.addAfter(method.copy(), locateElement);
                        psiClass.addBefore(whiteSpace, e);
                        method.delete();
                    }));
        }
    }

    protected Comparator<PsiMethod> getComparator() {
        return Comparator.comparing((PsiMethod o) -> hasModifierProperty(o, PsiModifier.ABSTRACT))
                .thenComparing((PsiMethod o) -> hasModifierProperty(o, PsiModifier.STATIC))
                .thenComparing(PsiMethod::isConstructor)
                .thenComparing((PsiMethod o) -> o.hasAnnotation(SCHEDULED_ANNOTATION_NAME))
                .thenComparingInt(this::getAccessOrder).reversed();
    }
}
