package com.ganguo.java.plugin.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ActionShowHelper {

    private final AnActionEvent event;
    private boolean enable = true;

    public static ActionShowHelper of(AnActionEvent event) {
        return new ActionShowHelper(Objects.requireNonNull(event));
    }

    private ActionShowHelper(AnActionEvent event) {
        this.event = event;
    }

    public ActionShowHelper fileNameMatch(String pattern) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getName)
                    .map(name -> name.matches(pattern))
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper fileNameEquals(String filename) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getName)
                    .map(name -> Objects.equals(name, filename))
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper filePathMatch(String pattern) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                    .map(VirtualFile::getPath)
                    .map(path -> path.matches(pattern))
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper fileMatch(Predicate<PsiFile> predicate) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE))
                    .map(predicate::test)
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper elementType(Class<? extends PsiElement> type) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                    .map(element -> type.isAssignableFrom(element.getClass()))
                    .orElse(false);
        }
        return this;
    }

    public <T extends PsiElement> ActionShowHelper elementMatch(Class<T> type, Predicate<T> predicate) {
        if (enable) {
            //noinspection unchecked
            enable = Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                    .filter(element -> type.isAssignableFrom(element.getClass()))
                    .map(element -> (T) element)
                    .map(predicate::test)
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper classWithAnnotation(String qName) {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE))
                    .filter(file -> file instanceof PsiJavaFile)
                    .map(file -> (PsiJavaFile) file)
                    .map(PsiUtils::getClassByFile)
                    .map(psiClass -> psiClass.hasAnnotation(qName))
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper isJavaFile() {
        if (enable) {
            enable = Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE))
                    .map(file -> file instanceof PsiJavaFile)
                    .orElse(false);
        }
        return this;
    }

    public ActionShowHelper isControllerApiMethod() {
        if (enable) {
            return fileNameMatch(".*Controller.java")
                    .elementMatch(PsiMethod.class, this::isApiMethod);
        }
        return this;
    }

    public ActionShowHelper and(Supplier<Boolean> supplier) {
        if (enable) {
            enable = supplier.get();
        }
        return this;
    }

    public boolean isShow() {
        return enable;
    }

    public void update() {
        event.getPresentation().setEnabled(enable);
    }

    private boolean isApiMethod(PsiMethod psiMethod) {
        return psiMethod.getAnnotation("org.springframework.web.bind.annotation.GetMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PostMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PutMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.DeleteMapping") != null;
    }
}
