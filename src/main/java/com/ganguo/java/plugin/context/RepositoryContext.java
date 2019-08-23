package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.constant.Paths;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.ImportThose;
import org.dependcode.dependcode.anno.Var;

import java.util.Optional;

@ImportThose(value = NewContext.class, data = "createModuleDir")
public class RepositoryContext {

    /**
     * domain文件夹
     */
    @Var
    public PsiDirectory domainDir(Context context, FuncAction<PsiDirectory> createModuleDir) {
        return createModuleDir.get(Paths.DOMAIN_REPOSITORY);
    }

    /**
     * Repository所在的文件夹
     */
    @Var
    public PsiDirectory infrastructureImplDir(PsiDirectoryFactory directoryFactory,
                                               VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }

    /**
     * DAO所在的文件夹
     */
    @Var
    public PsiDirectory infrastructureDbImplDir(PsiDirectoryFactory directoryFactory,
                                                 VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_DB_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }
}
