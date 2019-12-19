package com.ganguo.java.plugin.action.menu.format.sort;

import com.ganguo.java.plugin.context.RepositoryContext;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository方法排序
 */
@ImportFrom(RepositoryContext.class)
public class SortRepositoryAction extends SortMethodAction {
    private static final Order[] ORDERS = {
            Order.next("insert"),
            Order.next("batchInsert"),
            Order.next("update"),
            Order.next("batchUpdate"),
            Order.next("delete"),
            Order.next("query"),
            Order.next("find"),
            Order.next("get"),
            Order.next("select"),
            Order.next("list"),
            Order.next("page"),
            Order.next("exists"),
            Order.next("count"),
            Order.next("sum"),
    };

    @Func
    private void doAction(FuncAction<Void> sortSelectedClass, FuncAction<Void> sortRepositoryClasses,
                          WriteActions writeActions, @Nla String moduleName) {
        if (moduleName != null) {
            sortRepositoryClasses.exec();
        } else {
            sortSelectedClass.exec();
        }
        writeActions.run();
    }

    @Func
    private void sortSelectedClass(Project project, PsiClass selectedClass, WriteActions writeActions) {
        sortClasses(project, Collections.singletonList(selectedClass), writeActions);
    }

    @Func
    private void sortRepositoryClasses(Project project, PsiClass selectedClass,
                                       @Nla PsiClass daoClass, @Nla PsiClass implClass,
                                       WriteActions writeActions) {
        List<PsiClass> psiClasses = new ArrayList<>(3);
        psiClasses.add(selectedClass);
        if (daoClass != null) {
            psiClasses.add(daoClass);
        }
        if (implClass != null) {
            psiClasses.add(implClass);
        }
        sortClasses(project, psiClasses, writeActions);
    }

    @Override
    protected Comparator<PsiMethod> getComparator() {
        return super.getComparator()
                .thenComparing(this::getNameTypeOrder);
    }

    private int getNameTypeOrder(PsiMethod method) {
        String name = method.getName();
        for (Order order : ORDERS) {
            if (name.startsWith(order.prefix)) {
                return order.value;
            }
        }
        return 9999;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Order {
        private static int index = 1;

        private final String prefix;
        private final int value;

        public static Order next(String prefix) {
            return new Order(prefix, ++index);
        }

        public static Order same(String prefix) {
            return new Order(prefix, index);
        }
    }
}
