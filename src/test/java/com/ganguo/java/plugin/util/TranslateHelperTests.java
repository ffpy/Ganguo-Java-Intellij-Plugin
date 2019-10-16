package com.ganguo.java.plugin.util;

import org.junit.Test;

public class TranslateHelperTests {

    @Test
    public void test() throws Exception {
        String text = new TranslateHelper("20190927000337780", "pqqy1tyGffHGFmLVe2qE")
                .zh2En("普通++礼品卡++课程");
        System.out.println(text);
    }
}
