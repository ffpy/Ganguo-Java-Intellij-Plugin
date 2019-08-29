package com.ganguo.java.plugin.action.generate;

import com.ganguo.java.plugin.context.ControllerContext;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.ui.dialog.InputDialog;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.DefaultValue;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Nla;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 修改接口URL，并同步修改测试类
 */
@Slf4j
@ImportFrom({JavaFileContext.class, ControllerContext.class})
public class ModifyApiUrlAction extends BaseGenerateAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        Context context = ContextBuilder.of(new ModifyApiUrlAction())
                .put("newPathUrl", "")
                .put("event", mEvent)
                .build();
        String oldPathUrl = context.get("oldPathUrl", String.class);

        new InputDialog("请输入新的URL", "URL", oldPathUrl, "^[a-zA-Z0-9/-]*$", value -> {
            if (value.equals(oldPathUrl)) {
                return false;
            }
            return context.update("newPathUrl", value)
                    .exec("doAction", Boolean.class)
                    .orElse(false);
        }).show();
    }

    @Func
    private boolean doAction(FuncAction<Void> modifyMethodUrl, FuncAction<Void> modifyApiTestsUrl,
                             WriteActions writeActions, boolean apiTestFileExists) {
        modifyMethodUrl.exec();
        if (apiTestFileExists) {
            modifyApiTestsUrl.exec();
        }
        writeActions.run();
        return true;
    }

    @Func
    private void modifyMethodUrl(String newPathUrl, PsiAnnotation curMapping,
                                 PsiElementFactory elementFactory, WriteActions writeActions) {
        PsiExpression value = elementFactory.createExpressionFromText("\"" + newPathUrl + "\"", null);
        writeActions.add(() -> curMapping.setDeclaredAttributeValue("value", value));
    }

    @Func
    private void modifyApiTestsUrl(VirtualFile apiTestFile, String url, String newUrl,
                                   WriteActions writeActions) {
        try {
            Charset charset = apiTestFile.getCharset();
            String content = new String(apiTestFile.contentsToByteArray(), charset);
            String newContent = content.replace("\"" + url + "\"",
                    "\"" + newUrl + "\"");

            writeActions.add(() -> {
                try {
                    apiTestFile.setBinaryContent(newContent.getBytes(charset));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Var
    private String oldPathUrl(@Nla String pathUrl) {
        return pathUrl;
    }

    @Var
    private String newUrl(@DefaultValue("") String baseUrl, String newPathUrl) {
        return (baseUrl + newPathUrl).replace("//", "/");
    }

    @Override
    protected boolean isShow(AnActionEvent e) {
        return ActionShowHelper.of(e).isControllerApiMethod().isShow();
    }
}
