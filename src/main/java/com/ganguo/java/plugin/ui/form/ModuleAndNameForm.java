package com.ganguo.java.plugin.ui.form;

import com.ganguo.java.plugin.ui.BaseForm;

import javax.swing.*;

public class ModuleAndNameForm implements BaseForm {
    private JTextField moduleField;
    private JTextField nameField;
    private JPanel mainPanel;
    private JTextField pathField;
    private JLabel pathLabel;

    public JTextField getModuleField() {
        return moduleField;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getPathField() {
        return pathField;
    }

    public JLabel getPathLabel() {
        return pathLabel;
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
