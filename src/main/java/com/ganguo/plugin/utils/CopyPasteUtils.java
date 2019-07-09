package com.ganguo.plugin.utils;

import com.intellij.designer.clipboard.SimpleTransferable;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.DataFlavor;

public class CopyPasteUtils {

    public static void putString(String content) {
        CopyPasteManager.getInstance().setContents(new SimpleTransferable(content, DataFlavor.stringFlavor));
    }
}
