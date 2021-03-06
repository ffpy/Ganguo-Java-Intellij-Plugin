package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.util.FileUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.Ignore;
import org.dependcode.dependcode.anno.Var;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class NewContext {

    @Var
    public String modulePath(String module) {
        return module.replace('.', '/');
    }

    @Func
    public PsiDirectory createModuleDir(VirtualFile packageFile, PsiDirectoryFactory directoryFactory,
                                         String modulePath, @Ignore String path) {
        try {
            if (!path.endsWith("/")) {
                path += "/";
            }
            return Optional.ofNullable(
                    FileUtils.findOrCreateDirectory(packageFile, path + modulePath))
                    .map(directoryFactory::createDirectory)
                    .orElse(null);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
