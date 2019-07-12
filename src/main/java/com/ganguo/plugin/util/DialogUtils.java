package com.ganguo.plugin.util;

import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;

public class DialogUtils {

    public static ModuleAndName getModuleAndName() {
        String str = Messages.showInputDialog("", "请输入名称", null);
        if (StringUtils.isEmpty(str)) return null;

        String name;
        String module;

        int index = str.lastIndexOf(".");
        if (index == -1) {
            module = str;
            name = str;
        } else {
            module = str.substring(0, index);
            name = str.substring(index + 1);
        }

        if (StringUtils.isEmpty(module)) {
            MsgUtils.error("模块名不能为空");
            return null;
        }

        if (StringUtils.isEmpty(name)) {
            MsgUtils.error("名称不能为空");
            return null;
        }

        return new ModuleAndName(module.toLowerCase(), StringUtils.capitalize(name));
    }

    public static class ModuleAndName {
        private String module;
        private String name;

        public ModuleAndName(String module, String name) {
            this.module = module;
            this.name = name;
        }

        public String getModule() {
            return module;
        }

        public String getName() {
            return name;
        }

        public String getModulePath() {
            return module.replace('.', '/');
        }
    }
}
