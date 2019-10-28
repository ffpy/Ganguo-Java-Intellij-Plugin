package com.ganguo.java.plugin.ui.form;

import com.ganguo.java.plugin.ui.BaseForm;
import lombok.Getter;

import javax.swing.*;

@Getter
public class ModuleAndNameForm implements BaseForm {
    private JTextField moduleField;
    private JTextField nameField;
    private JPanel mainPanel;
    private JTextField pathField;
    private JLabel pathLabel;
}
