package com.ganguo.plugin.action.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseIntentionAction implements IntentionAction {

    private static final String FAMILY_NAME = "Ganguo";

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

}
