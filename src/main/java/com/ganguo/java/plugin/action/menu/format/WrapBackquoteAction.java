package com.ganguo.java.plugin.action.menu.format;

import com.ganguo.java.plugin.util.CharacterUtils;
import com.ganguo.java.plugin.util.SqlUtils;
import com.ganguo.java.plugin.action.BaseReplaceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * SQL字段用反引号包裹
 */
public class WrapBackquoteAction extends BaseReplaceAction {

    @Override
    protected String replace(AnActionEvent e, String text) throws Exception {
        StringBuilder newText = new StringBuilder(text.length());
        StringBuilder word = new StringBuilder();
        char prevChar = 0;
        // 是否位于单引号内，用于忽略单引号包含的文本单词
        boolean isInSingleQuote = false;
        // 是否在注释内，只判断'-- '注释
        boolean isInComment = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == '\'') {
                if (prevChar != '\\') {
                    isInSingleQuote = !isInSingleQuote;
                }
            }

            if (ch == '-') {
                char nextChar = (i + 1 >= text.length()) ? 0 : text.charAt(i + 1);
                if (prevChar == '-' && nextChar == ' ') {
                    isInComment = true;
                }
            }

            if (ch == '\n') {
                isInComment = false;
            }

            if (isSepChar(ch)) {
                appendWord(newText, word, !isInSingleQuote && !isInComment);
                newText.append(ch);
                word.delete(0, word.length());
            } else {
                word.append(ch);
            }

            prevChar = ch;
        }

        appendWord(newText, word, !isInSingleQuote && !isInComment);
        String newTextStr = newText.toString();

        return newTextStr.equals(text) ? null : newTextStr;
    }

    private boolean isAllLowerCase(StringBuilder str) {
        if (str.length() == 0) return false;

        if (CharacterUtils.isDigit(str.charAt(0))) return false;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '_') continue;

            if (!CharacterUtils.isLetterOrDigit(ch)) return false;
            if (CharacterUtils.isLetter(ch) && CharacterUtils.isUpperCase(ch)) return false;
        }
        return true;
    }

    private void appendWord(StringBuilder text, StringBuilder word, boolean enable) {
        if (enable && isAllLowerCase(word) && !SqlUtils.isMysqlKeyword(word.toString())) {
            text.append('`').append(word).append('`');
        } else {
            text.append(word);
        }
    }

    private boolean isSepChar(char ch) {
        return !CharacterUtils.isLetterOrDigit(ch) &&
                ch != '\'' && ch != '"' && ch != '`' && ch != '_';
    }
}
