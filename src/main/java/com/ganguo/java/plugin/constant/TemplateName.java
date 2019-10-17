package com.ganguo.java.plugin.constant;

/**
 * 模板名称
 */
public enum TemplateName {
    API_TEST_CLASS("ApiTestClass.vm"),
    DAO("DAO.vm"),
    I_DB_STRATEGY("IDbStrategy.vm"),
    I_REPOSITORY("IRepository.vm"),
    REPOSITORY("Repository.vm"),
    SERVICE("Service.vm"),
    SERVICE_IMPL("ServiceImpl.vm"),
    VALIDATION("Validation.vm"),
    VALIDATION_IMPL("ValidationImpl.vm"),
    ENUM_CODE("EnumCode.vm"),
    ;

    public static final String PATH_TEMPLATE = "/template/";

    public static String getPath(String name) {
        return PATH_TEMPLATE + name;
    }

    private final String name;

    TemplateName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return PATH_TEMPLATE + name;
    }
}
