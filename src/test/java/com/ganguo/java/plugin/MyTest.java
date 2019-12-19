package com.ganguo.java.plugin;

import com.intellij.psi.PsiModifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    @Test
    public void test2() {
        List<String> list = Arrays.asList(PsiModifier.PUBLIC, PsiModifier.PRIVATE,
                PsiModifier.PROTECTED, PsiModifier.DEFAULT);
        list.sort(String::compareTo);
        System.out.println(list);
    }
}
