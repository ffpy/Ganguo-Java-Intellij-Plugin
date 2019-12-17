package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.IndexUtils;
import com.ganguo.java.plugin.util.StringHelper;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetterSetterContext {

    private static final Pattern INDENT_PATTERN = Pattern.compile("(\\s*).*");

    @Var
    private PsiClass targetClass(Project project) {
        return Optional.ofNullable(CopyPasteManager.getInstance().getContents())
                .filter(contents -> contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                .map(contents -> {
                    try {
                        return (String) contents.getTransferData(DataFlavor.stringFlavor);
                    } catch (UnsupportedFlavorException | IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .map(name -> name.contains(".") ? IndexUtils.getClassByQualifiedName(project, name) :
                        IndexUtils.getClassByShortName(project, name))
                .orElse(null);
    }

    @Var
    private String varName(Editor editor) {
        return Optional.of(editor.getSelectionModel())
                .filter(SelectionModel::hasSelection)
                .map(SelectionModel::getSelectedText)
                .orElse(null);
    }

    @Var
    private String indent(Editor editor) {
        String indent = null;
        Matcher matcher = INDENT_PATTERN.matcher(EditorUtils.getCurLineText(editor));
        if (matcher.find()) {
            indent = matcher.group(1);
        }
        if (indent == null) {
            indent = "";
        }
        return indent;
    }

    @Var
    private String callers(Stream<PsiMethod> methodStream, String indent, String varName) {
        String callers = methodStream
                .map(method -> StringHelper.of("{var}.{methodName}();")
                        .param("var", varName)
                        .param("methodName", method.getName())
                        .toString())
                .distinct()
                .map(call -> indent + call + "\n")
                .collect(Collectors.joining());
        return callers.endsWith("\n") ? callers.substring(0, callers.length() - "\n".length()) : callers;
    }

    @Func
    private void doAction(Editor editor, String callers) {
        EditorUtils.insertToNextLine(editor, "\n" + callers);
    }
}
