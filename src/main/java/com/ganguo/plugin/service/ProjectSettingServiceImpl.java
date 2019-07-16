package com.ganguo.plugin.service;

import com.ganguo.plugin.constant.TemplateName;
import com.ganguo.plugin.util.ProjectUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.sun.istack.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class ProjectSettingServiceImpl implements ProjectSettingService {

    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_TEMPLATE = "template_";

    private final Project mProject;
    private final PropertiesComponent mProperties;

    public ProjectSettingServiceImpl(Project project) {
        mProject = project;
        mProperties = PropertiesComponent.getInstance(mProject);
    }

    @Override
    public String getPackageName() {
        String packageName = mProperties.getValue(KEY_PACKAGE_NAME);
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
            mProperties.unsetValue(KEY_PACKAGE_NAME);
        } else {
            mProperties.setValue(KEY_PACKAGE_NAME, packageName);
        }
    }

    @Override
    public String getTemplate(TemplateName name) {
        String template = mProperties.getValue(getTemplateKey(name));
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
            mProperties.unsetValue(getTemplateKey(name));
        } else {
            mProperties.setValue(getTemplateKey(name), template);
        }
    }

    private String getTemplateKey(TemplateName name) {
        return KEY_TEMPLATE + name.getName();
    }
}
