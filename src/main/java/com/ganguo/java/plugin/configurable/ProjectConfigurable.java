package com.ganguo.java.plugin.configurable;

import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.service.ProjectSettingService;
import com.ganguo.java.plugin.ui.form.ConfigurationForm;
import com.ganguo.java.plugin.util.PatternUtils;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class ProjectConfigurable implements SearchableConfigurable {

    private final ConfigurationForm mForm;
    private final ProjectSettingService mProjectSettingService;
    private Map<TemplateName, String> mTemplateMap;
    private String mPackageName;

    public ProjectConfigurable(Project project) {

        mProjectSettingService = ServiceManager.getService(project, ProjectSettingService.class);

        initTemplateMap();

        mForm = new ConfigurationForm(mTemplateMap);

        if (!ProjectUtils.isDefaultProject(project)) {
            mPackageName = mProjectSettingService.getPackageName();
        }

        mForm.onReset(e -> {
            if (Messages.showYesNoDialog("确认恢复默认设置？", "提示",
                    "确定", "取消", null) == Messages.YES) {
                mProjectSettingService.reset();
                mPackageName = mProjectSettingService.getPackageName();
                initTemplateMap();
                reset();
            }
        });
    }

    private void initTemplateMap() {
        mTemplateMap = Arrays.stream(TemplateName.values())
                .map(this::loadTemplate)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Item::getName, Item::getContent, (u, v) -> {
                    throw new IllegalStateException("Duplicate key");
                }, TreeMap::new));
    }

    @NotNull
    @Override
    public String getId() {
        return "com.ganguo.plugin";
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
        applyPackageName();
        applyTemplate();
    }

    private void applyTemplate() {
        Map<TemplateName, String> templateMap = mForm.getTemplateMap();
        templateMap.forEach(mProjectSettingService::setTemplate);
        mTemplateMap = templateMap;
    }

    private void applyPackageName() throws ConfigurationException {
        String packageName = mForm.getPackageNameField().getText();
        if (!PatternUtils.matchPackageName(packageName)) {
            throw new ConfigurationException("包名格式不正确");
        }

        mProjectSettingService.setPackageName(packageName);

        if (StringUtils.isEmpty(packageName)) {
            mForm.getPackageNameField().setText(mProjectSettingService.getPackageName());
        }

        mPackageName = mForm.getPackageNameField().getText();
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
        return Optional.ofNullable(mProjectSettingService.getTemplate(name))
                .map(content -> new Item<>(name, content))
                .orElse(null);
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private class Item<T> {
        private TemplateName name;
        private T content;
    }
}
