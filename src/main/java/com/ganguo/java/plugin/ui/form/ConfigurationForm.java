package com.ganguo.java.plugin.ui.form;

import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.ui.BaseForm;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigurationForm implements BaseForm {

    @Getter
    private JPanel mainPanel;

    @Getter
    private JTextField packageNameField;

    private JTabbedPane tabPane;

    private JButton resetButton;

    @Getter
    private JTextField translateAppIdField;

    @Getter
    private JTextField translateSecretField;

    private Map<TemplateName, Editor> editorMap = new HashMap<>();

    private EditorFactory editorFactory;

    /**
     * @param templateMap TreeMap实例
     */
    public ConfigurationForm(Map<TemplateName, String> templateMap) {
        editorFactory = EditorFactory.getInstance();
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");

        templateMap.entrySet().forEach(entry -> addTab(editorFactory, fileType, entry));

        if (tabPane.getTabCount() > 0) {
            tabPane.setSelectedIndex(0);
        }
    }

    public void onReset(ActionListener listener) {
        resetButton.addActionListener(Objects.requireNonNull(listener));
    }

    private void addTab(EditorFactory factory, FileType fileType, Map.Entry<TemplateName, String> entry) {
        EventQueue.invokeLater(() -> {
            Document document = factory.createDocument(entry.getValue());
            Editor editor = factory.createEditor(document, null, fileType, false);
            editorMap.put(entry.getKey(), editor);
            tabPane.addTab(entry.getKey().getName(), editor.getComponent());
        });
    }

    public Map<TemplateName, String> getTemplateMap() {
        return editorMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getDocument().getText()));
    }

    public void setTemplateMap(Map<TemplateName, String> templateMap) {
        for (Map.Entry<TemplateName, String> entry : templateMap.entrySet()) {
            Optional.ofNullable(editorMap.get(entry.getKey()))
                    .ifPresent(editor -> ApplicationManager.getApplication().runWriteAction(() ->
                            editor.getDocument().setText(entry.getValue())));
        }
    }

    public void dispose() {
        editorMap.values().forEach(editorFactory::releaseEditor);
        editorMap.clear();
        editorFactory = null;
    }
}
