package com.ganguo.java.plugin.util;

import org.apache.commons.lang3.StringUtils;

public class MyStringUtils {

    /**
     * 驼峰转下划线
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    public static String camelCase2UnderScoreCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder(str.length());

        boolean prevIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            boolean isUpperCase = Character.isUpperCase(ch);

            if (!prevIsUpperCase && isUpperCase) {
                sb.append('_');
            }
            sb.append(isUpperCase ? Character.toLowerCase(ch) : ch);

            prevIsUpperCase = !Character.isLetterOrDigit(ch) || isUpperCase;
        }

        return sb.toString();
    }

    /**
     * 下划线转驼峰
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    public static String underScoreCase2CamelCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder(str.length());

        boolean prevIsUnderline = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (ch == '_') {
                prevIsUnderline = true;
            } else {
                if (prevIsUnderline) {
                    sb.append(Character.toUpperCase(ch));
                    prevIsUnderline = false;
                } else {
                    sb.append(ch);
                }
            }
        }

        return sb.toString();
    }

    /**
     * 转换为标题样式，如hello_world -> HelloWorld
     * @param str 字符串
     * @return 转换后的字符串
     */
    public static String toTitle(String str) {
        return StringUtils.capitalize(underScoreCase2CamelCase(str));
    }
}
