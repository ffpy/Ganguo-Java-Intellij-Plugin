package com.ganguo.plugin.util;

import com.sun.istack.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SqlUtils {

    /** SQL关键字列表 */
    private static String[] MYSQL_KEYWORDS = {
            // 数据类型
            "tinyint", "smallint", "mediumint", "int", "integer", "bigint", "float",
            "double", "decimal", "date", "time", "year", "datetime", "timestamp",
            "char", "varchar", "tinyblob", "tinytext", "blob", "text", "mediumblob",
            "mediumtext", "longblob", "longtext",

            // 语句
            "select", "insert", "delete", "update", "table", "create", "drop", "alter",
            "create", "view", "index", "procedure", "trigger", "schema", "domain",
            "grant", "deny", "revoke", "commit", "rollback", "transaction", "ignore", "key",

            // 函数
            "ascii", "ord", "conv", "bin", "oct", "hex", "concat", "length", "octet_length",
            "char_length", "character_length", "locate", "left", "right", "substring", "mid",
            "ltrim", "rtrim", "trim", "space", "replace", "repeat", "reverse", "abs", "mod",
            "floor", "ceiling", "round", "exp", "log", "log10", "pow", "power", "dayofweek",
            "weekday", "dayofmonth", "dayofyear", "year", "hour", "date_format", "time_format",
            "now", "from_unixtime", "unix_timestamp", "sec_to_time", "time_to_sec"

    };

    private static Set<String> MySqlKeywordSet = new HashSet<>();

    static {
        Collections.addAll(MySqlKeywordSet, MYSQL_KEYWORDS);
        MYSQL_KEYWORDS = null;
    }

    /**
     * 判断字符串是不是MySQL的关键字
     *
     * @param word 字符串
     * @return true为是，false为否
     */
    public static boolean isMysqlKeyword(@Nullable String word) {
        if (word == null) return false;
        return MySqlKeywordSet.contains(word.toLowerCase());
    }
}
