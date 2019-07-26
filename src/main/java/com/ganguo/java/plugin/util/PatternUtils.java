package com.ganguo.java.plugin.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PatternUtils {

    private static final Predicate<String> PREDICATE_PACKAGE_NAME
            = Pattern.compile("^[\\w.]*$").asPredicate();

    /**
     * 判断字符串符不符合包名格式
     *
     * @param str 字符串
     * @return true为符合，false为不符合
     */
    public static boolean matchPackageName(String str) {
        return PREDICATE_PACKAGE_NAME.test(str);
    }
}
