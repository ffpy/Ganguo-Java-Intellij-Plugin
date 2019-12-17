package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 方法排序
 */
@Slf4j
@ImportFrom(JavaFileContext.class)
public class SortMethodAction extends BaseSortAction {

    @Func
    private void doAction(Project project, PsiClass selectedClass, WriteActions writeActions) {
        PsiField[] fields = selectedClass.getFields();
        PsiElement locateElement = fields.length == 0 ? selectedClass.getLBrace() : fields[fields.length - 1];
        PsiElement whiteSpace = PsiUtils.createWhiteSpace(project);

        Arrays.stream(selectedClass.getMethods())
                .sorted(Comparator.comparing(this::getMethodOrder)
                        .reversed()
                        .thenComparing(PsiMethod::getName)
                        .reversed())
                .forEachOrdered(method -> writeActions.add(() -> {
                    PsiElement e = selectedClass.addAfter(method.copy(), locateElement);
                    selectedClass.addBefore(whiteSpace, e);
                    method.delete();
                }));
        writeActions.run();
    }

    private int getMethodOrder(PsiMethod method) {
        return getOrder(method.getModifierList());
    }
}
