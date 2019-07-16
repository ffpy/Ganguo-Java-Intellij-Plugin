package com.ganguo.plugin.action.menu;

import com.ganguo.plugin.action.BaseReplaceAction;
import com.ganguo.plugin.util.SqlUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * SQL字段用反引号包裹
 */
public class WrapBackquoteAction extends BaseReplaceAction {

    /** 单词分隔字符 */
    private static final String SEP_CHARS = " ,.;()=";

    @Override
    protected String replace(AnActionEvent e, String text) throws Exception {
        StringBuilder newText = new StringBuilder(text.length());
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (isSepChar(ch)) {
                appendWord(newText, word);
                newText.append(ch);
                word.delete(0, word.length());
            } else {
                word.append(ch);
            }
        }

        appendWord(newText, word);
        String newTextStr = newText.toString();

        return newTextStr.equals(text) ? null : newTextStr;
    }

    private boolean isAllLowerCase(StringBuilder str) {
        if (str.length() == 0) return false;

        if (Character.isDigit(str.charAt(0))) return false;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '_') continue;

            if (!Character.isLetterOrDigit(ch)) return false;
            if (Character.isLetter(ch) && Character.isUpperCase(ch)) return false;
        }
        return true;
    }

    private void appendWord(StringBuilder text, StringBuilder word) {
        if (isAllLowerCase(word) && !SqlUtils.isMysqlKeyword(word.toString())) {
            text.append('`').append(word).append('`');
        } else {
            text.append(word);
        }
    }

    private boolean isSepChar(char ch) {
        for (int i = 0; i < SEP_CHARS.length(); i++) {
            if (SEP_CHARS.charAt(i) == ch) return true;
        }
        return false;
    }
}
