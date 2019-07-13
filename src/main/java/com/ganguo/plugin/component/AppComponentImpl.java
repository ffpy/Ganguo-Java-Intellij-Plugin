package com.ganguo.plugin.component;

import com.ganguo.plugin.action.intention.DeleteMsgAction;
import com.intellij.codeInsight.intention.IntentionManager;
import org.jetbrains.annotations.NotNull;

public class AppComponentImpl implements AppComponent {

    @NotNull
    @Override
    public String getComponentName() {
        return "GanGuo.AppComponent";
    }

    @Override
    public void initComponent() {
        IntentionManager.getInstance().addAction(new DeleteMsgAction());
    }
}
