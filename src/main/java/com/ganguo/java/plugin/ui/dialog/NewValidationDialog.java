package com.ganguo.java.plugin.ui.dialog;

import com.ganguo.java.plugin.ui.form.NewValidationForm;
import com.ganguo.java.plugin.ui.utils.InputLimit;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.EditorTextField;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NewValidationDialog extends BaseDialog<NewValidationForm, NewValidationDialog.Action> {

    private static final String DEFAULT_TYPE = "java.lang.Long";
    private static final int TYPE_FIELD_WIDTH = 250;

    private final AnActionEvent event;
    private InputLimit nameLimit;
    private EditorTextField typeField;

    public NewValidationDialog(AnActionEvent e, Action action) {
        super("生成校验注解", new NewValidationForm(), action);
        event = e;
        initComponent(e.getProject(), e);
    }

    private void initComponent(Project project, AnActionEvent e) {
        nameLimit = new InputLimit(mForm.getNameField(), "^([a-zA-Z][a-zA-Z\\d]*)?$");
        createTypeField(project);
    }

    private void createTypeField(Project project) {
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage("");
        PsiJavaCodeReferenceCodeFragment fragment = JavaCodeFragmentFactory.getInstance(project)
                .createReferenceCodeFragment(DEFAULT_TYPE, psiPackage, true, true);
        Document document = PsiDocumentManager.getInstance(project).getDocument(fragment);

        typeField = new EditorTextField(document, project, JavaFileType.INSTANCE);
        typeField.setSize(new Dimension(TYPE_FIELD_WIDTH, -1));
        mForm.getTypeFieldPanel().setLayout(new BorderLayout());
        mForm.getTypeFieldPanel().add(typeField, BorderLayout.CENTER);
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mForm.getNameField();
    }

    @Override
    protected void doOKAction() {
        String path = mForm.getPathField().getText().trim();
        String name = StringUtils.capitalize(mForm.getNameField().getText().trim());
        String type = typeField.getText().trim();

        if (!name.isEmpty() && !type.isEmpty() && nameLimit.getMatcher().test(name) &&
                mAction.apply(event, path, name, type)) {
            super.doOKAction();
        }
    }

    public interface Action extends DialogAction {

        /**
         * 执行OK动作
         *
         * @param event AnActionEvent
         * @param path  路径
         * @param name  名称
         * @param type  类型
         */
        boolean apply(AnActionEvent event, String path, String name, String type);
    }
}
