package com.ganguo.java.plugin.service;

import com.ganguo.java.plugin.constant.TemplateName;
import com.sun.istack.Nullable;

public interface ProjectSettingService {

    String getPackageName();

    void setPackageName(@Nullable String packageName);

    String getTemplate(TemplateName name);

    void setTemplate(TemplateName name, @Nullable String content);

    void reset();
}
