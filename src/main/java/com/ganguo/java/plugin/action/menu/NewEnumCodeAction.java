package com.ganguo.java.plugin.action.menu;

import com.ganguo.java.plugin.action.BaseAnAction;
import com.ganguo.java.plugin.constant.TemplateName;
import com.ganguo.java.plugin.context.JavaFileContext;
import com.ganguo.java.plugin.util.ActionShowHelper;
import com.ganguo.java.plugin.util.EditorUtils;
import com.ganguo.java.plugin.util.FileUtils;
import com.ganguo.java.plugin.util.MyStringUtils;
import com.ganguo.java.plugin.util.NotificationHelper;
import com.ganguo.java.plugin.util.ProjectUtils;
import com.ganguo.java.plugin.util.TranslateHelper;
import com.ganguo.java.plugin.util.WriteActions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dependcode.dependcode.ContextBuilder;
import org.dependcode.dependcode.FuncAction;
import org.dependcode.dependcode.anno.Func;
import org.dependcode.dependcode.anno.ImportFrom;
import org.dependcode.dependcode.anno.Var;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 生成EnumCode
 */
@Slf4j
@ImportFrom(JavaFileContext.class)
public class NewEnumCodeAction extends BaseAnAction {

    @Override
    protected void action(AnActionEvent e) throws Exception {
        ContextBuilder.of(this)
                .put("event", e)
                .build()
                .execVoid("doAction");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .fileNameMatch(".*\\.sql")
                .and(() -> getLineText(e).matches("\\s*`\\w+`\\s+TINYINT\\(1\\)\\s+.*COMMENT\\s+'.+:.*'.*"))
                .update();
    }

    @Func
    private void doAction(Project project, WriteActions writeActions, PsiDirectory directory, PsiFile file) {
        writeActions.add(() -> {
            FileUtils.addIfAbsent(directory, file);
            FileUtils.navigateFile(project, directory, file.getName());
        }).run();
    }

    /**
     * 当前行内容
     */
    @Var
    private String lineText(AnActionEvent event) {
        return getLineText(event);
    }

    /**
     * 字段名
     */
    @Var
    private String name(String lineText) {
        Pattern pattern = Pattern.compile("^\\s*`(\\w+)`");
        Matcher matcher = pattern.matcher(lineText);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 注释
     */
    @Var
    private String comment(String lineText) {
        Pattern pattern = Pattern.compile("COMMENT\\s+'.*:(.+)'");
        Matcher matcher = pattern.matcher(lineText);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 类名
     */
    @Var
    private String className(String name, String tableName) {
        return MyStringUtils.toTitle(tableName.toLowerCase()) +
                MyStringUtils.toTitle(name.toLowerCase());
    }

    /**
     * 模板参数
     */
    @Var
    private Map<String, Object> params(String className, List<Item> items) {
        Map<String, Object> params = new HashMap<>();
        params.put("className", className);
        params.put("items", items);
        return params;
    }

    /**
     * 项目列表
     */
    @Var
    private List<Item> items(String comment, TranslateHelper translateHelper) {
        List<Item> items = Arrays.stream(StringUtils.split(comment, ",，"))
                .map(item -> {
                    String[] split = StringUtils.split(item, "-");
                    return split.length == 2 ? new Item(split[0], split[1], split[1]) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 翻译
        try {
            String separator = ".";
            String text = items.stream().map(Item::getName).reduce((s1, s2) -> s1 + separator + s2)
                    .orElse("");
            String result = translateHelper.zh2En(text);
            String[] names = StringUtils.split(result, separator);
            for (int i = 0; i < names.length && i < items.size(); i++) {
                String name = names[i];
                if (StringUtils.isNotEmpty(name)) {
                    items.get(i).setName(name.trim().toUpperCase().replaceAll("[^\\w]+", "_"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return items;
    }

    /**
     * 表名
     */
    @Var
    private String tableName(Editor editor) {
        CaretModel caretModel = editor.getCaretModel();
        int line = caretModel.getLogicalPosition().line;
        Document document = editor.getDocument();
        Pattern pattern = Pattern.compile("CREATE\\s+TABLE\\s+`(\\w+)`");

        while (line > 0) {
            String text = EditorUtils.getLineText(document, --line);
            if (text == null) {
                return null;
            }
            if (text.matches(".*CREATE\\s+TABLE.*")) {
                Matcher matcher = pattern.matcher(text);
                return matcher.find() ? matcher.group(1) : null;
            }
        }
        return null;
    }

    /**
     * 文件
     */
    @Var
    private PsiFile file(FuncAction<PsiFile> createJavaFile, String filename,
                         Project project, PsiDirectory directory) {
        if (directory.findFile(filename + ".java") != null) {
            NotificationHelper.info("%s已存在", filename).show();
            FileUtils.navigateFile(project, directory, filename + ".java");
            return null;
        }
        return createJavaFile.get(TemplateName.ENUM_CODE, filename);
    }

    /**
     * 文件夹
     */
    @Var
    private PsiDirectory directory(Project project, PsiDirectoryFactory directoryFactory) {
        return Optional.ofNullable(ProjectUtils.getPackageFile(project))
                .map(file -> file.findFileByRelativePath("/constant"))
                .map(directoryFactory::createDirectory)
                .orElse(null);
    }

    /**
     * 文件名
     */
    @Var
    private String filename(String className) {
        return className;
    }

    /**
     * 获取当前行内容
     */
    @NotNull
    private String getLineText(@NotNull AnActionEvent e) {
        return Optional.ofNullable(e.getData(LangDataKeys.EDITOR))
                .map(EditorUtils::getCurLineText)
                .orElse("");
    }

    /**
     * 翻译工具类
     */
    @Var
    private TranslateHelper translateHelper(Project project) {
        return new TranslateHelper(project);
    }

    @Data
    @AllArgsConstructor
    public static class Item {

        /** 码值 */
        private String code;

        /** 字段名 */
        private String name;

        /** 注释 */
        private String comment;
    }
}
