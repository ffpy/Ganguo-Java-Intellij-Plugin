package com.ganguo.plugin.ui.form;

import com.ganguo.plugin.ui.BaseForm;

import javax.swing.*;

public class ModuleAndNameForm implements BaseForm {
    private JTextField mModuleField;
    private JTextField mNameField;
    private JPanel mainPanel;

    public JTextField getModuleField() {
        return mModuleField;
    }

    public JTextField getNameField() {
        return mNameField;
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }
}