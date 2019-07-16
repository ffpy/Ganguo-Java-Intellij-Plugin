package com.ganguo.plugin.ui.form;

import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.ui.BaseForm;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigurationForm implements BaseForm {

    private JPanel mainPanel;
    private JTextField mPackageNameField;
    private JTabbedPane mTabPane;
    private Map<TemplateName, Editor> mEditorMap = new HashMap<>();
    private EditorFactory mEditorFactory;

    /**
     * @param templateMap TreeMap实例
     */
    public ConfigurationForm(Map<TemplateName, String> templateMap) {
        mEditorFactory = EditorFactory.getInstance();
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");

        templateMap.entrySet().forEach(entry -> addTab(mEditorFactory, fileType, entry));

        if (mTabPane.getTabCount() > 0) {
            mTabPane.setSelectedIndex(0);
        }
    }

    private void addTab(EditorFactory factory, FileType fileType, Map.Entry<TemplateName, String> entry) {
        EventQueue.invokeLater(() -> {
            Document document = factory.createDocument(entry.getValue());
            Editor editor = factory.createEditor(document, null, fileType, false);
            mEditorMap.put(entry.getKey(), editor);
            mTabPane.addTab(entry.getKey().getName(), editor.getComponent());
        });
    }

    public Map<TemplateName, String> getTemplateMap() {
        return mEditorMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getDocument().getText()));
    }

    public void setTemplateMap(Map<TemplateName, String> templateMap) {
        for (Map.Entry<TemplateName, String> entry : templateMap.entrySet()) {
            Optional.ofNullable(mEditorMap.get(entry.getKey()))
                    .ifPresent(editor -> editor.getDocument().setText(entry.getValue()));
        }
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getPackageNameField() {
        return mPackageNameField;
    }

    public void dispose() {
        mEditorMap.values().forEach(mEditorFactory::releaseEditor);
        mEditorMap.clear();
        mEditorFactory = null;
    }
}
