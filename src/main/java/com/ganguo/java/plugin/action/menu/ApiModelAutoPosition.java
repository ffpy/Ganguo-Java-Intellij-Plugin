package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAction;
import com.ganguo.java.plugin.context.JavaFileContext;
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

/**
 * ApiModel自动编号
 */
@Slf4j
@ImportFrom(JavaFileContext.class)
public class ApiModelAutoPosition extends BaseAction {

    private static final String API_MODEL_ANNOTATION_NAME = "io.swagger.annotations.ApiModel";
    private static final String API_MODEL_PROPERTY_ANNOTATION_NAME = "io.swagger.annotations.ApiModelProperty";
    private static final String POSITION_ATTR_NAME = "position";

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        showWithAnnotationOnClass(e, API_MODEL_ANNOTATION_NAME);
    }

    @Func
    private void doAction(PsiClass curClass, PsiElementFactory elementFactory, WriteActions writeActions) {
        int pos = 1;
        for (PsiField field : curClass.getFields()) {
            PsiAnnotation anno = field.getAnnotation(API_MODEL_PROPERTY_ANNOTATION_NAME);
            if (anno != null) {
                PsiExpression value = elementFactory.createExpressionFromText(String.valueOf(pos++), null);
                writeActions.add(() -> anno.setDeclaredAttributeValue(POSITION_ATTR_NAME, value));
            }
        }
        writeActions.run();
    }
}
