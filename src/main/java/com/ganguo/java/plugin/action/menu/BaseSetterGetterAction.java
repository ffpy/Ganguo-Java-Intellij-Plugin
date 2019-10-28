package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.context.GetterSetterContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Optional;

@ForceImportFrom(GetterSetterContext.class)
public class BaseSetterGetterAction extends BaseAnAction {

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
}
