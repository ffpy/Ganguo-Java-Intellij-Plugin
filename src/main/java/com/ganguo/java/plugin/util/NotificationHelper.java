package com.ganguo.java.plugin.util;

import com.ganguo.java.plugin.constant.Constant;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

import java.util.Objects;

/**
 * 通知工具类
 */
public class NotificationHelper {

    private static final String TITLE_ERROR = "错误";
    private static final String TITLE_WARN = "警告";
    private static final String TITLE_INFO = "信息";


    private String title;
    private String content;
    private NotificationType type;
    private NotificationListener listener;

    public static NotificationHelper error(String content, Object... param) {
        return new NotificationHelper()
                .type(NotificationType.ERROR)
                .title(TITLE_ERROR)
                .contentWithParam(content, param);
    }

    public static NotificationHelper warn(String content, Object... param) {
        return new NotificationHelper()
                .type(NotificationType.WARNING)
                .title(TITLE_WARN)
                .contentWithParam(content, param);
    }

    public static NotificationHelper info(String content, Object... param) {
        return new NotificationHelper()
                .type(NotificationType.INFORMATION)
                .title(TITLE_INFO)
                .contentWithParam(content, param);
    }

    /**
     * 设置标题
     *
     * @param title 标题
     * @return this
     */
    public NotificationHelper title(String title) {
        this.title = Objects.requireNonNull(title);
        return this;
    }

    /**
     * 设置内容，支持HTML
     *
     * @param content 内容
     * @return this
     */
    public NotificationHelper content(String content) {
        this.content = Objects.requireNonNull(content);
        return this;
    }

    /**
     * 设置内容，支持HTML
     *
     * @param content 内容
     * @return this
     */
    public NotificationHelper contentWithParam(String content, Object... param) {
        this.content = String.format(Objects.requireNonNull(content), param);
        return this;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     * @return this
     */
    public NotificationHelper type(NotificationType type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    /**
     * 设置监听器
     *
     * @param listener 监听器
     * @return this
     */
    public NotificationHelper listener(NotificationListener listener) {
        this.listener = Objects.requireNonNull(listener);
        return this;
    }

    /**
     * 显示通知
     */
    public void show() {
        Notifications.Bus.notify(new Notification(Constant.GROUP_ID, title, content, type, listener));
    }
}
