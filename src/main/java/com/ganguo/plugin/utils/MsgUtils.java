package com.ganguo.plugin.utils;

import com.intellij.openapi.ui.Messages;

public class MsgUtils {

    public static final String TITLE_INFO = "提示";
    public static final String TITLE_WARN = "警告";
    public static final String TITLE_ERROR = "错误";

    public static void info(String message, Object... param) {
        Messages.showInfoMessage(String.format(message, param), TITLE_INFO);
    }

    public static void warn(String message, Object... param) {
        Messages.showWarningDialog(String.format(message, param), TITLE_WARN);
    }

    public static void error(String message, Object... param) {
        Messages.showErrorDialog(String.format(message, param), TITLE_ERROR);
    }
}
