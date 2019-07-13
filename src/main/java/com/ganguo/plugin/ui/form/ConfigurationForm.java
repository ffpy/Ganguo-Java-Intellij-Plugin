package com.ganguo.plugin.ui.form;

import com.ganguo.plugin.ui.BaseForm;
import com.ganguo.plugin.util.TemplateUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.io.FileUtil;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ConfigurationForm implements BaseForm {

    private final String[] templateNames = {
            "ApiTestClass.vm", "DAO.vm", "IDbStrategy.vm", "IRepository.vm",
            "Repository.vm", "Service.vm", "ServiceImpl.vm", "Validation.vm",
            "ValidationImpl.vm"
    };

    private JPanel mainPanel;
    private JTextField mPackageNameField;

    public ConfigurationForm() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Arrays.stream(templateNames)
                .map(name -> new Item<>(name, "/template" + name))
                .map(item -> {
                    try {
                        return new Item<>(item.getName(),
                                FileUtil.loadTextAndClose(
                                        TemplateUtils.class.getResourceAsStream(item.getContent())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(item -> {
                    Document document = editorFactory.createDocument(item.getContent());
                    Editor editor = editorFactory.createEditor(document, null, FileTypeManager.getInstance()
                            .getFileTypeByExtension("vm"), false);
//                    mTabPane.addTab(item.getName(), editor.getComponent());
                });
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }

    private class Item<T> {
        private String name;
        private T content;

        public Item(String name, T content) {
            this.name = name;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public T getContent() {
            return content;
        }
    }
}
