package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.IndexUtils;
import com.ganguo.java.plugin.util.PsiUtils;
import com.ganguo.java.plugin.util.StringHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 生成Setter调用代码
 */
@Slf4j
public class GenerateSetterCaller extends BaseAnAction {

    private static final Pattern INDENT_PATTERN = Pattern.compile("(\\s*).*");

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Transferable contents = CopyPasteManager.getInstance().getContents();
        //noinspection ConstantConditions
        ActionShowHelper.of(e)
                .and(() -> contents != null)
                .and(() -> contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                .and(() -> Optional.ofNullable(e.getData(LangDataKeys.EDITOR))
                        .map(Editor::getSelectionModel)
                        .map(SelectionModel::hasSelection)
                        .orElse(false))
                .update();
    }

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
    private String callers(PsiClass targetClass, String indent, String varName) {
        String callers = PsiUtils.getAllSetter(targetClass)
                .map(method -> StringHelper.of("{var}.{methodName}();")
                        .param("var", varName)
                        .param("methodName", method.getName())
                        .toString())
                .map(call -> indent + call + "\n")
                .collect(Collectors.joining());
        return callers.endsWith("\n") ? callers.substring(0, callers.length() - "\n".length()) : callers;
    }

    @Func
    private void doAction(Editor editor, String callers) {
        EditorUtils.insertToNextLine(editor, "\n" + callers);
    }
}
