package com.ganguo.plugin.ui;

import javax.swing.*;

public class NewRepositoryForm implements BaseForm {
    private JTextField mModuleField;
    private JTextField mNameField;
    private JTextField mTableField;
    private JPanel mainPanel;

    public JTextField getModuleField() {
        return mModuleField;
    }

    public JTextField getNameField() {
        return mNameField;
    }

    public JTextField getTableField() {
        return mTableField;
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
