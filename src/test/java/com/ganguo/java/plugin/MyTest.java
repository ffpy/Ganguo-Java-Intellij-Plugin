package com.ganguo.java.plugin;

import org.junit.Test;

import java.util.regex.Pattern;

public class MyTest {

    @Test
    public void test() {
        String content = "abc\n123\456123";
        String content2 = "abc\456123";
        Pattern compile = Pattern.compile("^123", Pattern.MULTILINE);
        System.out.println(compile.matcher(content).find());
        System.out.println(compile.matcher(content2).find());
    }
}
