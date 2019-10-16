package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.MyStringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 格式化SQL脚本
 */
public class FormatSqlAction extends BaseAnAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        Document doc = editor.getDocument();

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .fileNameMatch(".*\\.sql")
                .and(() -> e.getData(LangDataKeys.EDITOR) != null)
                .update();
    }

    /**
     * 格式化INSERT语句
     */
    public String formatInsert(String sql) {
        Pattern pattern = Pattern.compile("^([^(]*?)\\(([\\s\\S]*)\\)\\s*(VALUES)\\s*([\\s\\S]*)$",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }

        String insertStatement = matcher.group(1).trim();
        String columnsStr = matcher.group(2).trim();
        String valueKeyword = matcher.group(3).trim();
        String valuesStr = matcher.group(4).trim();

        String[] columns = getColumns(columnsStr);

        List<String[]> values = getValues(valuesStr);

        int[] lengths = getLengths(columns, values);

        // 填补字段空白
        fillColumnsBlank(columns, lengths);

        // 填补值空白
        fileValuesBlank(values, lengths);

        columnsStr = MyStringUtils.wrapWithBrackets(StringUtils.join(columns, ", "));
        valuesStr = StringUtils.join(values.stream()
                .map(item -> MyStringUtils.wrapWithBrackets(StringUtils.join(item, ", ")))
                .toArray(String[]::new), ",\n");

        if (StringUtils.isNotEmpty(valuesStr)) {
            valuesStr += ";";
        }

        return insertStatement + "\n" + columnsStr + "\n" + valueKeyword + "\n" + valuesStr;
    }

    @NotNull
    private String[] getColumns(String columnsStr) {
        return Arrays.stream(StringUtils.split(columnsStr, ","))
                .map(column -> column.trim().replace("`", ""))
                .map(column -> MyStringUtils.wrap(column, "`"))
                .toArray(String[]::new);
    }

    @NotNull
    private List<String[]> getValues(String valuesStr) {
        return Arrays.stream(MyStringUtils.split(valuesStr, "),", "'\""))
                .map(String::trim)
                .map(it -> Arrays.stream(MyStringUtils.split(it, ",", "'\""))
                        .map(String::trim)
                        .map(str -> str.startsWith("(") ? str.substring(1) : str)
                        .map(str -> str.endsWith(");") ? str.substring(0, str.length() - 2) : str)
                        .toArray(String[]::new))
                .collect(Collectors.toList());
    }

    private int[] getLengths(String[] columns, List<String[]> values) {
        int[] lengths = new int[columns.length];
        for (int i = 0; i < lengths.length; i++) {
            int finalI = i;
            lengths[i] = Math.max(MyStringUtils.lengthWithChinese(columns[i]), values.stream()
                    .map(it -> finalI < it.length ? MyStringUtils.lengthWithChinese(it[finalI]) : 0)
                    .max(Comparator.comparingInt(Integer::intValue))
                    .orElse(0));
        }
        return lengths;
    }

    private void fillColumnsBlank(String[] columns, int[] lengths) {
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            int len = lengths[i];
            int n = len - MyStringUtils.lengthWithChinese(column);
            if (n > 0) {
                columns[i] = column + StringUtils.repeat(' ', n);
            }
        }
    }

    private void fileValuesBlank(List<String[]> values, int[] lengths) {
        values.forEach(value -> {
            for (int i = 0; i < value.length; i++) {
                String item = value[i];
                int len = lengths[i];
                int n = len - MyStringUtils.lengthWithChinese(item);
                if (n > 0) {
                    if (item.contains("'") || item.contains("\"")) {
                        value[i] = item + StringUtils.repeat(' ', n);
                    } else {
                        value[i] = StringUtils.repeat(' ', n) + item;
                    }
                }
            }
        });
    }
}
