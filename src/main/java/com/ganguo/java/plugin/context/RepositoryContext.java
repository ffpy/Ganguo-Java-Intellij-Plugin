package com.ganguo.java.plugin.context;

import com.ganguo.java.plugin.constant.Paths;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.dependcode.dependcode.Context;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Var;

import java.util.Optional;

public class RepositoryContext {
    private static final Context CONTEXT = ContextBuilder.of(new RepositoryContext())
            .importThose(NewContext.getContext(), "createModuleDir")
            .build();

    public static Context getContext() {
        return CONTEXT;
    }

    /**
     * domain文件夹
     */
    @Var
    private PsiDirectory domainDir(Context context, FuncAction<PsiDirectory> createModuleDir) {
        return createModuleDir.get(Paths.DOMAIN_REPOSITORY);
    }

    /**
     * Repository所在的文件夹
     */
    @Var
    private PsiDirectory infrastructureImplDir(PsiDirectoryFactory directoryFactory,
                                               VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }

    /**
     * DAO所在的文件夹
     */
    @Var
    private PsiDirectory infrastructureDbImplDir(PsiDirectoryFactory directoryFactory,
                                                 VirtualFile packageFile) {
        return Optional.ofNullable(packageFile.findFileByRelativePath(Paths.INFRASTRUCTURE_DB_IMPL))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }
}
