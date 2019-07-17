package com.ganguo.plugin.component;

import com.ganguo.plugin.action.intention.DeleteMsgAction;
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
        IntentionManager.getInstance().addAction(new DeleteMsgAction());
        log.info("错误日志位置：{}", new File("log/GanguoPlugin.log").getAbsolutePath());
    }
}
