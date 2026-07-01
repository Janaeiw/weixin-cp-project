# Backend Database Guidelines

## Tech Stack
- MySQL 8.x, MyBatis-Plus 3.5.x, Spring Boot 3.x

---

## ORM：MyBatis-Plus

### Entity 定义规范

```java
@Data
@TableName("t_customer")
public class Customer extends BaseEntity {
    @TableId(type = IdType.AUTO)  // 自增 ID（避免雪花算法在 JS 端精度丢失）
    private Long id;

    private String name;

    @TableField("wx_user_id")
    private String wxUserId;

    @TableLogic  // 逻辑删除
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### BaseEntity — 所有表必须继承

```java
@Data
public class BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)  // 自增 ID（雪花算法超出 JS Number 精度）
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

---

## 命名规范

| 对象 | 规范 | 示例 |
|------|------|------|
| 表名 | `t_` 前缀 + 下划线小写 | `t_customer`, `t_wx_message` |
| 字段名 | 下划线小写 | `user_id`, `create_time` |
| 主键 | `id`，BIGINT，AUTO_INCREMENT | 禁止雪花算法（超出 JS Number 精度） |
| 逻辑删除字段 | `deleted`，INT，0=正常 1=删除 | — |
| 创建时间 | `create_time`，DATETIME | — |
| 更新时间 | `update_time`，DATETIME | — |
| 外键引用 | `关联表名_id` | `customer_id` |

---

## 查询规范

### 简单查询 — 直接用 MyBatis-Plus Wrapper

```java
List<Customer> list = customerMapper.selectList(
    new LambdaQueryWrapper<Customer>()
        .like(StringUtils.isNotBlank(name), Customer::getName, name)
        .eq(Customer::getDeleted, 0)
        .orderByDesc(Customer::getCreateTime)
);
```

### 复杂查询 — 写 XML Mapper

- 多表 JOIN、子查询、动态条件组合 → 放 `resources/mapper/` 下的 XML 文件
- XML namespace 必须与 Mapper 接口全限定名一致

### 分页查询 — 必须用 MyBatis-Plus 分页插件

```java
IPage<CustomerVO> page = customerMapper.selectPage(
    new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
    queryWrapper
);
```

---

## Migration 规范

- SQL 脚本放 `src/main/resources/db/migration/`
- 命名：`V{版本号}__{描述}.sql`，如 `V1.0.0__init_customer_table.sql`
- 每次结构变更必须提供对应 migration 脚本
- 禁止直接在线上数据库执行 DDL

---

## 严禁事项

1. **禁止拼接 SQL 字符串** — 防 SQL 注入
2. **禁止 N+1 查询** — 关联数据用 JOIN 或批量查询
3. **禁止 `SELECT *`** — 必须明确指定查询字段
4. **禁止循环中执行数据库操作** — 批量操作使用 `insertBatch` / `updateBatchById`
5. **禁止硬编码数据库连接信息** — 使用 `application-{profile}.yml` 配置
6. **禁止雪花算法 ID** — 必须使用 `IdType.AUTO`（雪花 ID 超出 JavaScript Number 精度 2^53）

---

## 二进制存储模式（图片存数据库）

当前项目使用数据库 LONGBLOB 存储图片（t_image 表），而非 OSS/文件系统。

### t_image 表结构

```sql
CREATE TABLE IF NOT EXISTS `t_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(255) NOT NULL COMMENT '文件名',
    `type`        VARCHAR(64)  DEFAULT NULL COMMENT 'MIME类型',
    `data`        LONGBLOB     NOT NULL COMMENT '图片二进制数据',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片存储表';
```

### Image 实体

```java
@Data
@TableName("t_image")
public class Image {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private byte[] data;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

### 接口约定

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/library/image/upload` | POST | 需要 | multipart/form-data，返回 `{id, url}` |
| `/api/library/image/{id}` | GET | 不需要 | 返回二进制图片，Content-Type 根据 t_image.type |

**注意**：图片访问接口必须在 SecurityConfig 中 permitAll。

### 前端上传 API

```typescript
export const uploadImage = (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return http.request<ApiResult<UploadResult>>("post", "/api/library/image/upload", {
    data: formData,
    headers: { "Content-Type": "multipart/form-data" }
  });
};
```
