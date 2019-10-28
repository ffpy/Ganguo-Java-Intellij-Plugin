package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.context.GetterSetterContext;
import com.ganguo.java.plugin.util.PsiUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.dependcode.dependcode.anno.Var;

import java.util.stream.Stream;

/**
 * 生成Setter调用代码
 */
@Slf4j
@ForceImportFrom(GetterSetterContext.class)
public class GenerateGetterCallerAction extends BaseSetterGetterAction {

    @Var
    private Stream<PsiMethod> methodStream(PsiClass targetClass) {
        return PsiUtils.getAllGetter(targetClass)
                .filter(method -> !method.getName().equals("getClass"));
    }
}
