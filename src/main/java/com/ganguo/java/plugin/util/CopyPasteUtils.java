package com.ganguo.java.plugin.util;

import com.intellij.designer.clipboard.SimpleTransferable;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.DataFlavor;

public class CopyPasteUtils {

    public static void putString(String content) {
        CopyPasteManager.getInstance().setContents(new SimpleTransferable(content, DataFlavor.stringFlavor));
    }
}
