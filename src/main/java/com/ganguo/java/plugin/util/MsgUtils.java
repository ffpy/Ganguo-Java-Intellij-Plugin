package com.ganguo.java.plugin.util;

import com.intellij.openapi.ui.Messages;

public class MsgUtils {

    private static final String TITLE_INFO = "提示";
    private static final String TITLE_WARN = "警告";
    private static final String TITLE_ERROR = "错误";

    public static void info(String message, Object... param) {
        Messages.showInfoMessage(String.format(String.valueOf(message), param), TITLE_INFO);
    }

    public static void warn(String message, Object... param) {
        Messages.showWarningDialog(String.format(String.valueOf(message), param), TITLE_WARN);
    }

    public static void error(String message, Object... param) {
        Messages.showErrorDialog(String.format(String.valueOf(message), param), TITLE_ERROR);
    }
}
