package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.MyStringUtils;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.apache.commons.collections.CollectionUtils;
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

    private static final Pattern PATTERN_FORMAT_INSERT = Pattern.compile(
            "^([^(]*?)\\(([\\s\\S]*)\\)\\s*(VALUES)\\s*([\\s\\S]*)$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        Document doc = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();
        boolean hasSelection = selectionModel.hasSelection();
        int selectionStart;
        int selectionEnd;
        String text;
        if (hasSelection) {
            // 选择模式
            selectionStart = selectionModel.getSelectionStart();
            selectionEnd = selectionModel.getSelectionEnd();
            text = selectionModel.getSelectedText();
        } else {
            // 全文模式
            selectionStart = 0;
            selectionEnd = 0;
            text = doc.getText();
        }

        String resultText = applyInsert(text);

        // 写入文件
        new WriteActions(e.getProject()).add(() -> {
            if (hasSelection) {
                doc.replaceString(selectionStart, selectionEnd, resultText);
            } else if (!text.equals(resultText)) {
                doc.setText(resultText);
            }
        }).run();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .fileNameMatch(".*\\.sql")
                .and(() -> e.getData(LangDataKeys.EDITOR) != null)
                .update();
    }

    /**
     * 处理插入语句
     *
     * @param text 源文本
     * @return 处理结果文本
     */
    private String applyInsert(String text) {
        final String endStr = ");";
        final String insertStatement = "INSERT";
        StringBuilder resultText = new StringBuilder();
        int index = 0;
        int from = 0;
        do {
            index = MyStringUtils.indexOf(text, insertStatement, index, "\"'", true);
            if (index != -1) {
                // 忽略注释
                if (MyStringUtils.lineStartsWith(text, "--", index) ||
                        MyStringUtils.lineStartsWith(text, "#", index)) {
                    index += insertStatement.length();
                    continue;
                }

                int start = index;
                int end = index;
                do {
                    end = MyStringUtils.indexOf(text, endStr, end, "\"'", false);
                    if (!MyStringUtils.lineStartsWith(text, "--", end) &&
                            !MyStringUtils.lineStartsWith(text, "#", end)) {
                        break;
                    }
                } while (end != -1);

                if (end != -1) {
                    resultText.append(text, from, index);

                    end += endStr.length();
                    index = end;
                    from = end;
                    String sql = text.substring(start, end);
                    resultText.append(formatInsert(sql));
                } else {
                    break;
                }
            }
        } while (index >= 0 && index < text.length());

        if (from < text.length()) {
            resultText.append(text, from, text.length());
        }
        return resultText.toString();
    }

    /**
     * 格式化INSERT语句
     */
    private String formatInsert(String sql) {
        Matcher matcher = PATTERN_FORMAT_INSERT.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }

        String insertStatement = matcher.group(1).trim();
        String columnsStr = matcher.group(2).trim();
        String valueKeyword = matcher.group(3).trim();
        String valuesStr = matcher.group(4).trim();

        String[] columns = getColumns(columnsStr);
        List<String[]> values = getValues(valuesStr);
        int[] widths = getWidths(columns, values);
        boolean[] isLeftAlign = getIsLeftAlign(values);

        fillColumnsBlank(columns, widths);
        fillValuesBlank(values, widths, isLeftAlign);

        columnsStr = MyStringUtils.wrapWithBrackets(StringUtils.join(columns, ", "));

        valuesStr = StringUtils.join(values.stream()
                .map(item -> MyStringUtils.wrapWithBrackets(StringUtils.join(item, ", ")))
                .toArray(String[]::new), ",\n").trim();
        if (StringUtils.isNotEmpty(valuesStr)) {
            valuesStr += ";";
        }

        return insertStatement + "\n" + columnsStr + "\n" + valueKeyword + "\n" + valuesStr;
    }

    /**
     * 获取字段
     */
    private String[] getColumns(String columnsStr) {
        return Arrays.stream(StringUtils.split(columnsStr, ","))
                .map(column -> column.trim().replace("`", ""))
                .map(column -> MyStringUtils.wrap(column, "`"))
                .toArray(String[]::new);
    }

    /**
     * 获取值列表
     */
    private List<String[]> getValues(String valuesStr) {
        return Arrays.stream(MyStringUtils.split(valuesStr, ",", "'\"", 0))
                .map(String::trim)
                .map(line -> {
                    if (line.startsWith("(")) {
                        line = line.substring(1);
                    }
                    if (line.endsWith(");")) {
                        line = line.substring(0, line.length() - 2);
                    } else if (line.endsWith(")")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    return line;
                })
                .map(String::trim)
                .map(it -> Arrays.stream(
                        MyStringUtils.split(it, ",", "'\"", 0))
                        .map(String::trim)
                        .toArray(String[]::new))
                .collect(Collectors.toList());
    }

    /**
     * 判断列是否为字符串类型
     */
    private boolean[] getIsLeftAlign(List<String[]> values) {
        if (CollectionUtils.isEmpty(values)) {
            return EMPTY_BOOLEAN_ARRAY;
        }

        boolean[] arrays = new boolean[values.get(0).length];
        for (String[] value : values) {
            for (int i = 0; i < value.length && i < arrays.length; i++) {
                String item = value[i];
                if (!"NULL".equalsIgnoreCase(item) && !item.matches("\\d+")) {
                    arrays[i] = true;
                }
            }
        }

        return arrays;
    }

    /**
     * 计算各列的最大宽度
     */
    private int[] getWidths(String[] columns, List<String[]> values) {
        int[] lengths = new int[columns.length];
        for (int i = 0; i < lengths.length; i++) {
            int finalI = i;
            lengths[i] = Math.max(MyStringUtils.width(columns[i]), values.stream()
                    .map(it -> finalI < it.length ? MyStringUtils.width(it[finalI]) : 0)
                    .max(Comparator.comparingInt(Integer::intValue))
                    .orElse(0));
        }
        return lengths;
    }

    /**
     * 填充字段空白
     */
    private void fillColumnsBlank(String[] columns, int[] lengths) {
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            int len = lengths[i];
            int n = len - MyStringUtils.width(column);
            if (n > 0) {
                columns[i] = column + StringUtils.repeat(' ', n);
            }
        }
    }

    /**
     * 填充值空白
     */
    private void fillValuesBlank(List<String[]> values, int[] lengths, boolean[] isLeftAlign) {
        values.forEach(value -> {
            for (int i = 0; i < value.length; i++) {
                String item = value[i];
                int len = lengths[i];
                int n = len - MyStringUtils.width(item);
                if (n > 0) {
                    if (i < isLeftAlign.length && isLeftAlign[i]) {
                        value[i] = item + StringUtils.repeat(' ', n);
                    } else {
                        value[i] = StringUtils.repeat(' ', n) + item;
                    }
                }
            }
        });
    }
}
