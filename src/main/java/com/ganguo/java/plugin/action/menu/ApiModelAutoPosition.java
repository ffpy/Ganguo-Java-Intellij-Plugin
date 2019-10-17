package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.constant.AnnotationNames;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ApiModel自动编号
 */
@Slf4j
@ImportFrom(JavaFileContext.class)
public class ApiModelAutoPosition extends BaseAnAction {

    private static final String POSITION_ATTR_NAME = "position";
    private static final String HIDDEN_ATTR_NAME = "hidden";

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .classWithAnnotation(AnnotationNames.API_MODEL)
                .update();
    }

    @Func
    private void doAction(PsiClass curClass, PsiElementFactory elementFactory, WriteActions writeActions) {
        int pos = 1;
        for (PsiField field : curClass.getFields()) {
            PsiAnnotation anno = field.getAnnotation(AnnotationNames.API_MODEL_PROPERTY);
            if (anno != null) {
                Boolean hidden = PsiUtils.getAnnotationValue(anno, HIDDEN_ATTR_NAME, Boolean.class);
                PsiExpression value;
                if (Objects.equals(hidden, true)) {
                    value = null;
                } else {
                    value = elementFactory.createExpressionFromText(String.valueOf(pos++), null);
                }
                writeActions.add(() -> anno.setDeclaredAttributeValue(POSITION_ATTR_NAME, value));
            }
        }
        writeActions.run();
    }
}
