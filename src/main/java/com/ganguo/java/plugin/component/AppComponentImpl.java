package com.ganguo.java.plugin.component;

import com.ganguo.java.plugin.action.intention.DeleteExceptionMsgAction;
import com.ganguo.java.plugin.action.intention.InsertTimestampAction;
import com.intellij.codeInsight.intention.IntentionManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class AppComponentImpl implements AppComponent {

    @NotNull
    @Override
    public String getComponentName() {
        return "GanGuo.AppComponent";
    }

    @Override
    public void initComponent() {
        log.info("错误日志位置：{}", new File("log/GanguoPlugin.log").getAbsolutePath());

        IntentionManager manager = IntentionManager.getInstance();
        manager.addAction(new DeleteExceptionMsgAction());
        manager.addAction(new InsertTimestampAction());
    }
}
