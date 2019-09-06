package com.ganguo.java.plugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StringHelper {
    private static final String LEFT = "\001";
    private static final String RIGHT = "\002";

    private String mStr;
    private Map<String, Object> paramMap = new HashMap<>();

    public static StringHelper of(String str) {
        return new StringHelper(str);
    }

    public static StringHelper of(String str, Map<String, Object> params) {
        return of(str).params(params);
    }

    public static String toString(String str, Map<String, Object> params) {
        return of(str, params).toString();
    }

    private StringHelper(String str) {
        this.mStr = Objects.requireNonNull(str);
    }

    public StringHelper param(String name, Object value) {
        paramMap.put(name, value);
        return this;
    }

    public StringHelper params(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            paramMap.putAll(params);
        }
        return this;
    }

    @Override
    public String toString() {
        if (mStr.isEmpty()) return "";

        mStr = mStr.replace("{{", LEFT);
        mStr = mStr.replace("}}", RIGHT);

        paramMap.forEach(
                (key, value) -> mStr = mStr.replace("{" + key + "}", String.valueOf(value)));

        mStr = mStr.replace(LEFT, "{");
        mStr = mStr.replace(RIGHT, "}");

        return mStr;
    }
}
