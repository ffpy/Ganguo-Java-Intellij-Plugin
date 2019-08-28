package com.ganguo.java.plugin.ui.form;

import com.ganguo.java.plugin.ui.BaseForm;

import javax.swing.*;

public class InputDialogForm implements BaseForm {
    private JTextField valueField;
    private JLabel keyField;
    private JPanel mainPanel;

    public JTextField getValueField() {
        return valueField;
    }

    public JLabel getKeyField() {
        return keyField;
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
