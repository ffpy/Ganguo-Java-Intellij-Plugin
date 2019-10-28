## 安装
1. 下载最新版的插件文件: [ganguo-java-intellij-plugin-v1.4](https://github.com/ffpy/Ganguo-Java-Intellij-Plugin/releases/download/v1.4/ganguo-java-intellij-plugin-1.4.zip)
2. 打开IntelliJ IDEA，选择Intellij IDEA->Preferences->Plugin->Install Plugin from Disk，
选择下载的插件文件
3. 重启IDEA

## 功能列表

### ExceptionMsg

#### 添加ExceptionMsg
选择`顶部菜单栏->Ganguo->添加ExceptionMsg`，在弹出的输入框中输入键和值，
按确定键后即可在`exception_msg.properties`和`ExceptionMsg.class`中添加对应的键和值。

#### 删除ExceptionMsg
在`exception_msg.properties`文件中把光标移到某一行上，按`Alt+Enter`，
在弹出的菜单中选择`删除Msg`即可删除对应的Msg。

### 生成代码

> 生成代码的模板可在`主菜单->Other Settings->Ganguo中修改`，模板是项目级别的，可以在不同的项目中设置不同的模板。

#### 生成Service接口及实现类
选择`顶部菜单栏->Ganguo->生成Service`，在弹出的输入框中输入`路径(api或admin)`、`模块名`和`名称`，
按确定键后即可生成对应的Service接口和Service实现类。<br>
例如：输入`(api, user, user)`，即可生成如下文件:
1. com.ganguomob.dev.xxxx.service.api.user.UserService
2. com.ganguomob.dev.xxxx.service.api.user.UserServiceImpl

#### 生成Repository接口及实现类
选择`顶部菜单栏->Ganguo->生成Repository`，在弹出的输入框中输入`表名`、`模块名`和`名称`，
按确定键后即可生成对应的IRepository接口、IDbStrategy接口、Repository实现类和DAO实现类。<br>
例如：输入`(USER, user, user)`，即可生成如下文件:
1. com.ganguomob.dev.xxxx.domain.repository.user.IUserRepository
2. com.ganguomob.dev.xxxx.domain.repository.user.IUserDbStrategy
3. com.ganguomob.dev.xxxx.infrastructure.repository.impl.UserRepository
4. com.ganguomob.dev.xxxx.infrastructure.repository.db.impl.UserDAO

#### 生成校验注解
选择`顶部菜单栏->Ganguo->生成校验注解`，在弹出的输入框中输入`路径`、`名称`和`类型`，
按确定键后即可生成对应的Validator注解和ValidatorImpl实现类。<br>
例如：输入`(user, UserExists, java.lang.Long)`，即可生成如下文件:
1. com.ganguomob.dev.xxxx.infrastructure.validation.user.UserExists
2. com.ganguomob.dev.xxxx.infrastructure.validation.user.UserExistsValidatorImpl

#### 生成接口测试类
在Controller类中右键点击`接口方法名`，在弹出的菜单中选择`甘果->生成测试类`即可生成对应的测试类。

#### 生成EnumCode
在sql文件中光标移到字段所在行，打开`顶部菜单栏->Ganguo->生成EnumCode`，即可生成对应的状态类。<br>
字段列必须匹配格式: \`xxx\` (TINY)?INT(x) ... COMMENT 'xx:0-xxx,1-xx'<br>
例如把光标移到status字段所在行，选择`生成EnumCode`。
```sql
CREATE TABLE `user`
(
    `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(64)         NOT NULL COMMENT '名称',
    `status`     TINYINT(1) UNSIGNED NOT NULL COMMENT '状态:0-正常,1-冻结',
    `created_at` BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
    `updated_at` BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
生成代码:
```java
@AllArgsConstructor
@Getter
public enum UserStatus implements EnumCode {

    /** 正常 */
    NORMAL(0),

    /** 冻结 */
    FROZEN(1),

    ;
    private final int code;
}
```
生成过程中会调用百度翻译API，需要在设置中配置百度翻译的应用ID和密钥。

### 格式化

#### 格式化SQL脚本
打开要格式化的SQL文件，选择`顶部菜单栏->Ganguo->格式化SQL脚本`，即可格式化当前SQL文件。也可以选中文本来部分格式化。<br>
目前仅支持INSERT语句格式化:
```sql
INSERT INTO `user`
(`id`, `name`, `phone`)
VALUES
(1, '小明', '13414850000'),
(2, 'Jack', '13414850001'),
(3, '小花', '8613414850001');
```
结果:
```sql
INSERT INTO `user`
(`id`, `name`, `phone`        )
VALUES
(   1, '小明' , '13414850000'  ),
(   2, 'Jack', '13414850001'  ),
(   3, '小花' , '8613414850001');
```

### 其他

#### 插入当前时间戳
`Alt+Enter`->插入当前时间戳

#### 驼峰-下划线命名方式互转
选中文本，选择`顶部菜单栏->Ganguo->驼峰-下划线互转`

#### SQL字段用反引号包裹
在SQL文件中选中要添加反引号的文本，选择`顶部菜单栏->Ganguo->SQL字段用反引号包裹`。<br>
```sql
CREATE TABLE user
(
    id         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64)         NOT NULL COMMENT '名称',
    created_at BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
    updated_at BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
结果:
```sql
CREATE TABLE `user`
(
    `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(64)         NOT NULL COMMENT '名称',
    `created_at` BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
    `updated_at` BIGINT(13) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 生成Getter调用
右键类名，选择`Copy Reference`，然后双击选中变量名，选择`顶部菜单栏->Ganguo->生成Getter调用`。
```java
User user = new User();
```
结果
```java
User user = new User();
user.getId();
user.getName();
user.getPassword();
user.getPhone();
```

#### 生成Setter调用
右键类名，选择`Copy Reference`，然后双击选中变量名，选择`顶部菜单栏->Ganguo->生成Setter调用`。
```java
User user = new User();
```
结果
```java
User user = new User();
user.setId();
user.setName();
user.setPassword();
user.setPhone();
```

#### 实现IRepository方法
在IXXXRepository类中，右键点击方法名，选择`甘果->实现此方法`。

#### ApiModel自动编号
打开标注了`@ApiModel`注解的类(如UserRequest)，选择`顶部菜单栏->Ganguo->ApiModel自动编号`。<br>
可自动给标注了`@ApiModelProperty`注解的字段添加`position`属性，并且按照字段属性进行编号。

#### Mapping生成ignore
打开标注了@Mapper注解的类(如UserAssembler)，右键点击`方法名`，选择`甘果->添加ignore`，
可自动添加`@Mapping(target = "xxx", ignore = true)`注解。

#### 接口测试类和接口方法互相跳转
- 在Controller类中右键点击`接口方法名`，在弹出的菜单中选择`甘果->跳转到测试类`即可跳转到对应的测试类。
- 在测试类中右键点击任意地方，在弹出的菜单中选择`甘果->跳转到接口方法`即可跳转到对应的接口方法。

#### 修改接口方法名称
在Controller类中右键点击`接口方法名`，在弹出的菜单中选择`甘果->修改方法名称`，输入新的方法名称，
可同时修改方法名和对应的测试类名。

#### 修改接口URL
在Controller类中右键点击`接口方法名`，在弹出的菜单中选择`甘果->修改方法URL`，输入新的URL，
可同时修改方法的URL和对应的测试类中引用的URL。
