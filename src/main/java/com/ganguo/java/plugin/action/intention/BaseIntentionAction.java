package com.ganguo.java.plugin.action.intention;

import com.ganguo.java.plugin.context.BaseContext;
import com.intellij.codeInsight.intention.IntentionAction;
import org.dependcode.dependcode.anno.ForceImportFrom;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

@ForceImportFrom(BaseContext.class)
public abstract class BaseIntentionAction implements IntentionAction {

    private static final String FAMILY_NAME = "Ganguo";

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

}
