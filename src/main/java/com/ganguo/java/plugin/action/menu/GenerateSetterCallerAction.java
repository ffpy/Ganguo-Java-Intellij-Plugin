package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.util.PsiUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.anno.Var;

import java.util.stream.Stream;

/**
 * 生成Setter调用代码
 */
@Slf4j
public class GenerateSetterCallerAction extends BaseSetterGetterAction {

    @Var
    private Stream<PsiMethod> methodStream(PsiClass targetClass) {
        return PsiUtils.getAllSetter(targetClass);
    }
}
