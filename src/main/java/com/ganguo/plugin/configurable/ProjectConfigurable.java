package com.ganguo.plugin.configurable;

import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.service.ProjectSettingService;
import com.ganguo.plugin.ui.form.ConfigurationForm;
import com.ganguo.plugin.util.PatternUtils;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ProjectConfigurable implements SearchableConfigurable {

    private ConfigurationForm mForm;
    private final ProjectSettingService mProjectSettingService;
    private Map<TemplateName, String> mTemplateMap;
    private String mPackageName;

    public ProjectConfigurable(Project project) {
        mProjectSettingService = ServiceManager.getService(project, ProjectSettingService.class);
        mTemplateMap = Arrays.stream(TemplateName.values())
                .map(this::loadTemplate)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Item::getName, Item::getContent, (u, v) -> {
                    throw new IllegalStateException("Duplicate key");
                }, TreeMap::new));

        mForm = new ConfigurationForm(mTemplateMap);
        mPackageName = mProjectSettingService.getPackageName();
    }

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
        return mForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        if (!mForm.getPackageNameField().getText().equals(mPackageName)) {
            return true;
        }
        if (!mForm.getTemplateMap().equals(mTemplateMap)) {
            return true;
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        // packageName
        String packageName = mForm.getPackageNameField().getText();
        if (!PatternUtils.matchPackageName(packageName)) {
            throw new ConfigurationException("包名格式不正确");
        }

        mProjectSettingService.setPackageName(packageName);

        if (StringUtils.isEmpty(packageName)) {
            mForm.getPackageNameField().setText(mProjectSettingService.getPackageName());
        }

        mPackageName = mForm.getPackageNameField().getText();

        // template
        Map<TemplateName, String> templateMap = mForm.getTemplateMap();
        templateMap.forEach(mProjectSettingService::setTemplate);
        mTemplateMap = templateMap;
    }

    @Override
    public void reset() {
        mForm.getPackageNameField().setText(mPackageName);
        mForm.setTemplateMap(mTemplateMap);
    }

    @Override
    public void disposeUIResources() {
        mForm.dispose();
    }

    private Item<String> loadTemplate(TemplateName name) {
        String template = mProjectSettingService.getTemplate(name);
        return Optional.ofNullable(template)
                .map(content -> new Item<>(name, content))
                .orElse(null);
    }

    private class Item<T> {
        private TemplateName name;
        private T content;

        public Item(TemplateName name, T content) {
            this.name = name;
            this.content = content;
        }

        public TemplateName getName() {
            return name;
        }

        public T getContent() {
            return content;
        }
    }
}
