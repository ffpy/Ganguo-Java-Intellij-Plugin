package com.ganguo.java.plugin.service;

import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.sun.istack.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;

public class SettingServiceImpl implements SettingService {

    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_TEMPLATE = "template_";
    private static final String KEY_TRANSLATE_APP_ID = "translate_app_id";
    private static final String KEY_TRANSLATE_SECRET = "translate_secret";

    private final Project mProject;
    private final PropertiesComponent projectProperties;
    private final PropertiesComponent applicationProperties;

    public SettingServiceImpl(Project project) {
        mProject = project;
        projectProperties = PropertiesComponent.getInstance(mProject);
        applicationProperties = PropertiesComponent.getInstance();
    }

    @Override
    public String getPackageName() {
        String packageName = projectProperties.getValue(KEY_PACKAGE_NAME);
        if (StringUtils.isEmpty(packageName)) {
            try {
                packageName = ProjectUtils.getPackageName(mProject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return packageName;
    }

    @Override
    public void setPackageName(@Nullable String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            projectProperties.unsetValue(KEY_PACKAGE_NAME);
        } else {
            projectProperties.setValue(KEY_PACKAGE_NAME, packageName);
        }
    }

    @Override
    public String getTranslateAppId() {
        return applicationProperties.getValue(KEY_TRANSLATE_APP_ID);
    }

    @Override
    public void setTranslateAppId(String appId) {
        applicationProperties.setValue(KEY_TRANSLATE_APP_ID, appId);
    }

    @Override
    public String getTranslateSecret() {
        return applicationProperties.getValue(KEY_TRANSLATE_SECRET);
    }

    @Override
    public void setTranslateSecret(String secret) {
        applicationProperties.setValue(KEY_TRANSLATE_SECRET, secret);
    }

    @Override
    public String getTemplate(TemplateName name) {
        String template = projectProperties.getValue(getTemplateKey(name));
        if (template == null) {
            try {
                template = FileUtil.loadTextAndClose(getClass().getResourceAsStream(name.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return template;
    }

    @Override
    public void setTemplate(TemplateName name, @Nullable String template) {
        if (template == null) {
            projectProperties.unsetValue(getTemplateKey(name));
        } else {
            projectProperties.setValue(getTemplateKey(name), template);
        }
    }

    @Override
    public void reset() {
        setPackageName(null);
        Arrays.stream(TemplateName.values())
                .forEach(name -> setTemplate(name, null));
    }

    private String getTemplateKey(TemplateName name) {
        return KEY_TEMPLATE + name.getName();
    }
}
