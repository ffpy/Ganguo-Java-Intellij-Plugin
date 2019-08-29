package com.ganguo.java.plugin.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ActionShowHelper {
    private static final ActionShowHelper EMPTY = new ActionShowHelper(null);

    private final AnActionEvent event;

    public static ActionShowHelper of(AnActionEvent event) {
        return new ActionShowHelper(Objects.requireNonNull(event));
    }

    private ActionShowHelper(AnActionEvent event) {
        this.event = event;
    }

    public ActionShowHelper fileNameMatch(String pattern) {
        if (this != EMPTY) {
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getName)
                    .map(name -> name.matches(pattern))
                    .orElse(false));
        }
        return this;
    }

    public ActionShowHelper fileNameEquals(String filename) {
        if (this != EMPTY) {
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getName)
                    .map(name -> Objects.equals(name, filename))
                    .orElse(false));
        }
        return this;
    }

    public ActionShowHelper filePathMatch(String pattern) {
        if (this != EMPTY) {
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getPath)
                    .map(path -> path.matches(pattern))
                    .orElse(false));
        }
        return this;
    }

    public ActionShowHelper fileMatch(Predicate<PsiFile> predicate) {
        if (this != EMPTY) {
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE))
                    .map(predicate::test)
                    .orElse(false));
        }
        return this;
    }

    public ActionShowHelper elementType(Class<? extends PsiElement> type) {
        if (this != EMPTY) {
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                    .map(element -> type.isAssignableFrom(element.getClass()))
                    .orElse(false));
        }
        return this;
    }

    public <T extends PsiElement> ActionShowHelper elementMatch(Class<T> type, Predicate<T> predicate) {
        if (this != EMPTY) {
            //noinspection unchecked
            return checkMatch(Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                    .filter(element -> type.isAssignableFrom(element.getClass()))
                    .map(element -> (T) element)
                    .map(predicate::test)
                    .orElse(false));
        }
        return this;
    }

    public ActionShowHelper isControllerApiMethod() {
        if (this != EMPTY) {
            return fileNameMatch(".*Controller.java")
                    .elementMatch(PsiMethod.class, this::isApiMethod);
        }
        return this;
    }

    public ActionShowHelper and(Supplier<Boolean> supplier) {
        if (this != EMPTY) {
            return checkMatch(supplier.get());
        }
        return this;
    }

    public boolean isShow() {
        return this != EMPTY;
    }

    public void update() {
        event.getPresentation().setEnabled(isShow());
    }

    private ActionShowHelper checkMatch(boolean match) {
        return match ? this : EMPTY;
    }

    private boolean isApiMethod(PsiMethod psiMethod) {
        return psiMethod.getAnnotation("org.springframework.web.bind.annotation.GetMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PostMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PutMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.DeleteMapping") != null;
    }
}
