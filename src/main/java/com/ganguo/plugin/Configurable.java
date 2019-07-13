package com.ganguo.plugin;

import com.ganguo.plugin.ui.form.ConfigurationForm;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Configurable implements SearchableConfigurable {

    @NotNull
    @Override
    public String getId() {
        return "Ganguo";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Ganguo";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return new ConfigurationForm().getMainPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
