# 后端代码结构与分层规范

## 1. 目标

这份规范用于统一 `AetherFlow-server` 的后端代码组织方式。

当前项目采用：

- 模块化单体
- 单服务部署
- 按业务模块拆包
- 模块内再按分层组织代码

核心原则：

- 先按模块划边界，再写具体业务
- 不把所有代码都堆进全局 `controller/service/mapper/domain`
- 公共能力进入 `common/config/exception`
- 业务代码优先进入 `modules`

## 2. 顶层包结构

当前推荐结构：

```text
com.berlin.aetherflow
  common
  config
  exception
  modules
    system
      auth
      monitor
      user
      role
      menu
    wms
      warehouse
      location
      material
      inbound
      outbound
      stock
      inventory
```

说明：

- `common`
  放通用基类、工具类、分页对象、基础审计字段等
- `config`
  放全局 Spring 配置、MVC 配置、序列化配置等
- `exception`
  放统一返回结构、错误码、全局异常处理
- `modules`
  放所有业务模块

## 3. 模块内部结构

每个业务模块内部统一按下面组织：

```text
modules/system/user
  constant
  controller
  domain
    bo
    entity
    vo
  enums
  mapper
  service
    impl
```

最小模块至少包含：

- `controller`
- `service`
- `mapper`
- `domain/entity`

需要参数对象或返回对象时，再补：

- `domain/bo`
- `domain/vo`

## 4. 代码注释规范

### 4.1 基本原则

当前项目不强制使用 `package-info.java`。

优先做好：

- 类注释
- 接口注释
- 关键方法注释
- 复杂逻辑前的简短说明

注释原则：

- 解释“为什么这样做”或“这段代码负责什么”
- 不写机械重复代码含义的废话
- 参数多、职责明显的方法优先补 Javadoc

### 4.2 推荐补注释的位置

- Controller 类
- Service 接口和关键业务方法
- 监听器、拦截器、配置类
- 复杂查询转换方法
- 有明显业务语义的 BO / VO / Entity

### 4.3 Javadoc 风格

推荐像下面这样写：

```java
/**
 * 将 T 类型对象，按照配置的映射字段规则，给 desc 类型的对象赋值并返回 desc 对象
 *
 * @param source 数据来源实体
 * @param desc   转换后的对象
 * @return desc
 */
```

要求：

- 先写一句完整说明
- 有参数时补 `@param`
- 有返回值时补 `@return`
- 只给真正有说明价值的方法写

## 5. 枚举类规范

目录：

- `modules/**/enums`

用途：

- 表达稳定、有限、可枚举的业务状态或类别

适合放进枚举的内容：

- 用户类型
- 单据状态
- 出入库类型
- 库存变更类型

不适合放进枚举的内容：

- 经常变化的配置项
- 需要从数据库维护的字典项

命名规范：

- 类名：`XxxType`、`XxxStatus`、`XxxMode`
- 枚举项：全大写下划线风格

示例：

```java
public enum UserType {
    ADMIN("admin", "管理员"),
    USER("user", "普通用户")
}
```

建议字段：

- `code`
- `description`

建议能力：

- 提供按 `code` 查找的静态方法
- 统一做合法性判断

## 6. 常量类规范

目录：

- `modules/**/constant`

用途：

- 放模块内重复使用的固定常量

适合放进常量类的内容：

- 角色标识
- 默认状态值
- 固定业务字符串

不适合放进常量类的内容：

- 实际上属于枚举的业务状态
- 会变动的配置值
- 与单一方法强绑定的局部常量

命名规范：

- 类名：`XxxConstants`
- 常量名：全大写下划线风格
- 常量类使用私有构造器

示例：

```java
public class UserConstants {
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    private UserConstants() {
    }
}
```

## 7. 实体类规范

目录：

- `modules/**/domain/entity`

用途：

- 对应数据库表结构
- 作为 MyBatis-Plus 持久化对象

规则：

- 一个实体类尽量只对应一张核心表
- 字段命名尽量与数据库字段语义一致
- 持久化相关注解写在实体类里
- 不要把请求参数、页面展示字段、复杂拼装字段直接塞进实体

适合放在实体里的内容：

- 数据库字段
- 主键定义
- 乐观锁、逻辑删除、自动填充等持久化字段

不适合放在实体里的内容：

- 前端页面临时字段
- 查询条件对象
- 聚合展示字段
- 复杂业务行为

建议：

- 继承公共审计基类时显式声明 `@EqualsAndHashCode(callSuper = true)`
- 敏感字段如密码应避免直接返回给前端

## 8. BO 规范

目录：

- `modules/**/domain/bo`

用途：

- 作为业务输入对象
- 承载创建、更新、查询条件等服务层输入参数

BO 可以理解为：

- 传给 `service` 的入参对象
- 比 `entity` 更贴近业务动作

适合放在 BO 的内容：

- 新建仓库请求参数
- 修改物料请求参数
- 入库单创建参数
- 查询筛选条件

规则：

- BO 不直接映射数据库表
- BO 可以加参数校验注解
- BO 字段按业务动作组织，不按数据库表机械复制

命名建议：

- `CreateWarehouseBo`
- `UpdateMaterialBo`
- `InboundOrderQueryBo`

## 9. VO 规范

目录：

- `modules/**/domain/vo`

用途：

- 作为接口输出对象
- 承载返回给前端的展示结构

适合放在 VO 的内容：

- 页面展示需要的聚合字段
- 脱敏后的返回字段
- 额外拼装出来的名称、状态文案、统计值

规则：

- VO 面向前端展示，不强绑定数据库表
- 一个接口可以返回一个专门的 VO
- 不要直接把 Entity 当成最终长期输出对象

命名建议：

- `UserDetailVo`
- `WarehouseListVo`
- `StockOverviewVo`

## 10. Controller 规范

目录：

- `modules/**/controller`

职责：

- 接收请求
- 参数校验
- 调用 Service
- 返回统一结果

不要在 Controller 里做：

- 复杂业务判断
- 大量数据转换
- 直接操作 Mapper
- 跨模块拼装复杂流程

规则：

- 一个 Controller 对应一个清晰资源领域
- 路由使用 REST 风格
- 返回统一 `Result<T>`
- 入参优先使用 BO，出参优先使用 VO

示例路由：

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/warehouses`
- `PUT /api/materials/{id}`

## 11. Service 与 Impl 规范

目录：

- `modules/**/service`
- `modules/**/service/impl`

职责划分：

- `service`
  放接口，定义业务能力
- `service/impl`
  放具体实现，承载业务逻辑

规则：

- Controller 只依赖 Service 接口
- Impl 中再组合 Mapper 和其他模块服务
- 事务注解优先写在 Service 实现层

不要在 Service 里做的事：

- 返回原始 SQL 结构
- 混入 HTTP 相关处理

不要在 Impl 里做的事：

- 把所有逻辑都变成 CRUD 透传
- 直接暴露数据库细节给 Controller

命名规范：

- 接口：`UserService`
- 实现：`UserServiceImpl`

## 12. Mapper 规范

目录：

- `modules/**/mapper`

用途：

- 持久化访问层
- 执行数据库读写

规则：

- 一个实体通常对应一个 Mapper
- Mapper 只做数据访问，不做业务编排
- 能用 MyBatis-Plus 通用能力解决的，优先不用手写 XML
- 复杂查询再落 XML

不要在 Mapper 里做：

- 跨多个业务模块的流程编排
- 业务规则校验

命名规范：

- `UserMapper`
- `WarehouseMapper`
- `InboundOrderMapper`

## 13. src/main/resources/mapper 规范

目录建议：

```text
src/main/resources/mapper
  system
    user
      UserMapper.xml
  wms
    warehouse
      WarehouseMapper.xml
```

不要把所有 XML 都平铺在 `mapper/` 根目录。

推荐按模块分文件夹，和 Java 包结构保持对应关系。

规则：

- XML 文件名与 Mapper 接口名一致
- `namespace` 必须等于 Mapper 接口全限定名
- 只把复杂 SQL 放进 XML
- 简单单表 CRUD 优先使用 MyBatis-Plus

示例：

```xml
<mapper namespace="com.berlin.aetherflow.modules.system.user.mapper.UserMapper">
</mapper>
```

适合写进 XML 的 SQL：

- 多表联查
- 复杂动态条件
- 统计聚合查询
- 性能要求较强、需要手控 SQL 的查询

不适合写进 XML 的 SQL：

- 简单按 ID 查
- 简单分页查询
- 通用插入、更新、删除

## 14. 依赖方向规范

推荐依赖方向：

```text
controller -> service -> mapper
controller -> service -> other module service
service -> mapper
service -> common
```

禁止：

- `controller -> mapper`
- `controller -> XML SQL`
- `module A controller -> module B mapper`

跨模块调用时，优先：

- `A 模块 Service -> B 模块 Service`

不要：

- `A 模块直接依赖 B 模块 Mapper`

## 15. 当前项目落地要求

从现在开始，新增业务代码按下面执行：

1. 先确定模块归属
2. 再创建模块目录
3. 再写 `entity / bo / vo / service / mapper / controller`
4. 最后给关键类和关键方法补注释

当前后续优先模块建议：

1. `modules/system/auth`
2. `modules/system/role`
3. `modules/system/menu`
4. `modules/wms/warehouse`
5. `modules/wms/location`
6. `modules/wms/material`

## 16. 一句话标准

如果一个类你无法在 10 秒内判断它应该属于哪个模块，说明当前命名、职责或分层还不够清晰。
