package com.ganguo.java.plugin.configurable;

import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.service.SettingService;
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

    private final ConfigurationForm form;
    private final SettingService settingService;
    private Map<TemplateName, String> templateMap;
    private String packageName;
    private String translateAppId;
    private String translateSecret;

    public ProjectConfigurable(Project project) {

        settingService = ServiceManager.getService(project, SettingService.class);

        initTemplateMap();

        form = new ConfigurationForm(templateMap);

        if (!ProjectUtils.isDefaultProject(project)) {
            packageName = settingService.getPackageName();
        }

        translateAppId = settingService.getTranslateAppId();
        translateSecret = settingService.getTranslateSecret();

        form.onReset(e -> {
            if (Messages.showYesNoDialog("确认恢复默认设置？", "提示",
                    "确定", "取消", null) == Messages.YES) {
                settingService.reset();
                packageName = settingService.getPackageName();
                initTemplateMap();
                reset();
            }
        });
    }

    private void initTemplateMap() {
        templateMap = Arrays.stream(TemplateName.values())
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
        return form.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return !form.getPackageNameField().getText().equals(packageName) ||
                !form.getTemplateMap().equals(templateMap) ||
                !form.getTranslateAppIdField().getText().equals(translateAppId) ||
                !form.getTranslateSecretField().getText().equals(translateSecret);
    }

    @Override
    public void apply() throws ConfigurationException {
        applyPackageName();
        applyTemplate();
        applyTranslateAppId();
        applyTranslateSecret();
    }

    private void applyTemplate() {
        Map<TemplateName, String> templateMap = form.getTemplateMap();
        templateMap.forEach(settingService::setTemplate);
        this.templateMap = templateMap;
    }

    private void applyPackageName() throws ConfigurationException {
        String packageName = form.getPackageNameField().getText();
        if (!PatternUtils.matchPackageName(packageName)) {
            throw new ConfigurationException("包名格式不正确");
        }

        settingService.setPackageName(packageName);

        if (StringUtils.isEmpty(packageName)) {
            form.getPackageNameField().setText(settingService.getPackageName());
        }

        this.packageName = form.getPackageNameField().getText();
    }

    private void applyTranslateAppId() {
        String translateAppId = form.getTranslateAppIdField().getText();
        settingService.setTranslateAppId(translateAppId);
        this.translateAppId = translateAppId;
    }

    private void applyTranslateSecret() {
        String translateSecret = form.getTranslateSecretField().getText();
        settingService.setTranslateSecret(translateSecret);
        this.translateSecret = translateSecret;
    }

    @Override
    public void reset() {
        form.getPackageNameField().setText(packageName);
        form.setTemplateMap(templateMap);
        form.getTranslateAppIdField().setText(translateAppId);
        form.getTranslateSecretField().setText(translateSecret);
    }

    @Override
    public void disposeUIResources() {
        form.dispose();
    }

    private Item<String> loadTemplate(TemplateName name) {
        return Optional.ofNullable(settingService.getTemplate(name))
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
