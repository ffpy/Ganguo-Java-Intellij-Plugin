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

import java.util.Comparator;

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
        sortClasses(project, new PsiClass[]{selectedClass}, writeActions);
    }

    @Func
    private void sortRepositoryClasses(Project project, PsiClass selectedClass, PsiClass daoClass, PsiClass implClass,
                                       WriteActions writeActions) {
        sortClasses(project, new PsiClass[]{selectedClass, implClass, daoClass}, writeActions);
    }

    @Override
    protected Comparator<PsiMethod> getComparator() {
        return Comparator.comparing(this::getMethodOrder).reversed()
                .thenComparing(this::getNameTypeOrder)
                .thenComparing(PsiMethod::getName).reversed();
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
