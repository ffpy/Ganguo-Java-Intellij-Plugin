package com.ganguo.java.plugin.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    public static String toTitle(String str) {
        return StringUtils.capitalize(underScoreCase2CamelCase(str));
    }

    /**
     * 分隔字符串
     *
     * @param str          要分隔的字符串
     * @param separator    分隔符
     * @param excludeFlags 要忽略的包裹字符，如"'，分隔的时候会忽略包裹字符
     *                     如split("abd'1,2'cd124,56", ",", "'") => [ "abd'1,2'cd124", "56" ]
     * @return 分隔结果
     */
    public static String[] split(String str, String separator, String excludeFlags) {
        return split(str, separator, excludeFlags, -1);
    }

    /**
     * 分隔字符串
     *
     * @param str             要分隔的字符串
     * @param separator       分隔符
     * @param excludeFlags    要忽略的包裹字符，如"'，分隔的时候会忽略包裹字符
     *                        如split("abd'1,2'cd124,56", ",", "'") => [ "abd'1,2'cd124", "56" ]
     * @param maxBracketDepth 生效的括号深度，-1表示忽略此选项
     * @return 分隔结果
     */
    public static String[] split(String str, String separator, String excludeFlags, int maxBracketDepth) {
        if (StringUtils.isEmpty(str)) {
            return new String[0];
        }
        if (StringUtils.isEmpty(separator)) {
            throw new IllegalArgumentException("separator cannot be empty");
        }
        if (StringUtils.isEmpty(excludeFlags)) {
            throw new IllegalArgumentException("excludeFlags cannot be empty");
        }

        List<String> list = new ArrayList<>();
        char flagChar = 0;
        int start = 0;
        int bracketDepth = 0;
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];

            if (excludeFlags.indexOf(ch) != -1) {
                if (flagChar == 0) {
                    flagChar = ch;
                } else if (ch == flagChar) {
                    flagChar = 0;
                }
            } else if (flagChar == 0) {
                if (ch == '(') {
                    bracketDepth++;
                } else if (ch == ')') {
                    bracketDepth--;
                }

                if (maxBracketDepth != -1 && maxBracketDepth >= bracketDepth) {
                    int end = i + 1;
                    if (end >= separator.length()) {
                        if (subEquals(charArray, separator, end - separator.length(), end)) {
                            list.add(str.substring(start, end - separator.length()));
                            start = end;
                        }
                    }
                }
            }
        }
        list.add(str.substring(start));

        return list.toArray(new String[list.size()]);
    }

    /**
     * 查找子字符串的位置
     *
     * @param str          原字符串
     * @param subStr       子字符串
     * @param fromIndex    开始查找的位置
     * @param excludeFlags 忽略的包裹字符
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    public static int indexOf(String str, String subStr, int fromIndex,
                              String excludeFlags) {
        return indexOf(str, subStr, fromIndex, excludeFlags, false);
    }

    /**
     * 查找子字符串的位置
     *
     * @param str             原字符串
     * @param subStr          子字符串
     * @param fromIndex       开始查找的位置
     * @param excludeFlags    忽略的包裹字符
     * @param caseInsensitive 是否忽略大小写
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    public static int indexOf(String str, String subStr, int fromIndex,
                              String excludeFlags, boolean caseInsensitive) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(subStr)) {
            return -1;
        }

        return indexOf(str.toCharArray(), subStr, fromIndex, excludeFlags, caseInsensitive);
    }

    /**
     * 查找子字符串的位置
     *
     * @param charArray    字符数组
     * @param subStr       子字符串
     * @param fromIndex    开始查找的位置
     * @param excludeFlags 忽略的包裹字符
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    public static int indexOf(char[] charArray, String subStr, int fromIndex, String excludeFlags) {
        return indexOf(charArray, subStr, fromIndex, excludeFlags, false);
    }

    /**
     * 查找子字符串的位置
     *
     * @param charArray       字符数组
     * @param subStr          子字符串
     * @param fromIndex       开始查找的位置
     * @param excludeFlags    忽略的包裹字符
     * @param caseInsensitive 是否忽略大小写
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    public static int indexOf(char[] charArray, String subStr, int fromIndex,
                              String excludeFlags, boolean caseInsensitive) {
        if (Objects.requireNonNull(charArray).length == 0 || StringUtils.isEmpty(subStr)) {
            return -1;
        }
        if (fromIndex < 0 || fromIndex >= charArray.length) {
            throw new IndexOutOfBoundsException();
        }
        if (StringUtils.isEmpty(excludeFlags)) {
            throw new IllegalArgumentException("excludeFlags cannot be empty");
        }

        char flagChar = 0;
        for (int i = fromIndex; i < charArray.length; i++) {
            char ch = charArray[i];
            if (excludeFlags.indexOf(ch) != -1) {
                if (flagChar == 0) {
                    flagChar = ch;
                } else if (ch == flagChar) {
                    flagChar = 0;
                }
            } else if (flagChar == 0) {
                int end = i + 1;
                if (end >= subStr.length()) {
                    if (subEquals(charArray, subStr, end - subStr.length(), end, caseInsensitive)) {
                        return end - subStr.length();
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 判断字符数组指定范围的字符串是否与给定字符串相同
     *
     * @param chars 字符数组
     * @param other 要比较的子字符串
     * @param start 开始位置
     * @param end   结束位置，不包括
     * @return true相同，false不同
     */
    public static boolean subEquals(char[] chars, String other, int start, int end) {
        return subEquals(chars, other, start, end, false);
    }

    /**
     * 判断字符数组指定范围的字符串是否与给定字符串相同
     *
     * @param chars           字符数组
     * @param other           要比较的子字符串
     * @param start           开始位置
     * @param end             结束位置，不包括
     * @param caseInsensitive 是否忽略大小写
     * @return true相同，false不同
     */
    public static boolean subEquals(char[] chars, String other, int start, int end, boolean caseInsensitive) {
        if (start < 0 || end < 0 || start > end) {
            throw new StringIndexOutOfBoundsException();
        }

        if (end - start != other.length()) {
            return false;
        }

        for (int i = start; i < end; i++) {
            if (caseInsensitive) {
                if (Character.toLowerCase(chars[i]) != Character.toLowerCase(other.charAt(i - start))) {
                    return false;
                }
            } else {
                if (chars[i] != other.charAt(i - start)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 包裹字符串，如wrap("ab", "'") => 'ab'
     *
     * @param str     要包裹的字符串
     * @param wrapStr 包裹字符
     * @return 结果字符串
     */
    public static String wrap(String str, String wrapStr) {
        return wrapStr + str + wrapStr;
    }

    /**
     * 用括号包裹字符串，如wrapWithBrackets("ab") => (ab)
     *
     * @param str 要包裹的字符串
     * @return 结果字符串
     */
    public static String wrapWithBrackets(String str) {
        return "(" + str + ")";
    }

    /**
     * 统计包括中文字符串的显示长度，一个中文字符的显示长度约为1.665个英文字符的显示长度
     *
     * @param str 字符串
     * @return 显示长度
     */
    public static int width(String str) {
        Pattern pattern = Pattern.compile("([\\u4E00-\\u9FA5]|[\\uFE30-\\uFFA0])");
        Matcher matcher = pattern.matcher(str);
        int n = 0;
        while (matcher.find()) {
            n++;
        }
        return str.length() + (int) Math.round(n * 0.665);
    }

    /**
     * 判断字符串中是否有中文（汉字/中文标点符号）
     *
     * @param str 字符串
     * @return true为有，false为没有
     */
    public static boolean hasChinese(String str) {
        Pattern pattern = Pattern.compile("([\\u4E00-\\u9FA5]|[\\uFE30-\\uFFA0])");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 判断指定位置所在行的前缀是否为子串
     *
     * @param str        字符串
     * @param prefix     前缀字符串
     * @param index      位置
     * @return true为是，false为否
     */
    public static boolean lineStartsWith(String str, String prefix, int index) {
        int lineStart = 0;
        for (int i = index; i >= 0; i--) {
            if (str.charAt(i) == '\n') {
                lineStart = i + 1;
                break;
            }
        }

        if (lineStart >= str.length()) {
            return false;
        }

        return str.startsWith(prefix, lineStart);
    }
}
