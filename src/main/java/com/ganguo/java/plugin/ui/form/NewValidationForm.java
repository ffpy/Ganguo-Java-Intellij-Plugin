package com.ganguo.java.plugin.ui.form;

import com.ganguo.java.plugin.ui.BaseForm;
import lombok.Getter;

import javax.swing.*;

@Getter
public class NewValidationForm implements BaseForm {
    private JTextField pathField;
    private JPanel mainPanel;
    private JTextField nameField;
    private JTextField typeField;
}
