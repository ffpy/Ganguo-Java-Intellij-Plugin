package com.ganguo.java.plugin.ui.utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class TypeEnterListener extends KeyAdapter {

    private final Runnable mRunnable;

    public TypeEnterListener(Runnable runnable) {
        this.mRunnable = Objects.requireNonNull(runnable);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            mRunnable.run();
        }
    }
}
