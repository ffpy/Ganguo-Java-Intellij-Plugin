package com.ganguo.plugin.action.generate;

import com.ganguo.plugin.util.ElementUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class GenerateRepositoryMethodAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {

    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return checkShow(e, "^I.*Repository.java$", ElementUtils::isMethodElement);
    }
}
