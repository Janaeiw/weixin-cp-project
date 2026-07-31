# 系统日志管理功能

## Goal

实现系统操作日志的自动记录、查询和详情查看功能，方便运营人员快速通过日志核查系统故障和操作问题。

## What I already know

- 项目框架：Spring Boot 3.3.5 + MyBatis-Plus 3.5.9 + MySQL
- 前端：Vue 3.5 + Element Plus 2.11 + vue-pure-admin
- 现有模式：Controller → SystemService → Mapper，统一响应 R<T>，分页 Page<T>
- 前端已有占位页面 `system/log/index.vue`，菜单已由 DataInitializer 种子化
- 后端无任何日志/审计相关实现
- SecurityConfig 中 `/api/system/**` 当前 permitAll()

## Assumptions (temporary)

- 日志只记录写操作（增删改），不记录查询
- 采用 AOP 注解方式自动记录，无需手动在每个 Controller 中调用
- 日志表只记录操作日志，不记录系统异常日志（异常日志走 logback 文件）

## Decision (ADR-lite)

**Context**: 需要选择日志记录方式和覆盖范围
**Decision**: AOP 全局拦截，pointcut 匹配所有 Controller 的 POST/PUT/DELETE 方法，自动记录；可选 `@OperationLog` 注解仅用于补充操作描述
**Consequences**: 全模块覆盖，新增接口自动纳入，零配置；仅 GET 不记录（查询无副作用）；方法级自定义描述通过可选注解实现

## Open Questions

- 需要记录哪些操作模块的写操作？

## Requirements (evolving)

- [ ] 数据库建表 `t_operation_log`
- [ ] AOP 切面拦截写操作，自动记录日志
- [ ] 后端 API：分页查询、查看详情、删除
- [ ] 前端页面：搜索筛选 + 列表 + 查看详情弹窗

## Acceptance Criteria (evolving)

- [ ] 新增/修改/删除操作自动写入日志表
- [ ] 日志列表支持按操作类型、操作人、时间范围筛选
- [ ] 点击可查看详情（请求参数、响应结果、异常信息等）
- [ ] 日志只可查看和删除，不可修改

## Definition of Done

- 前后端功能完整可用
- Lint / type-check 通过
- 与现有代码风格一致

## Out of Scope (explicit)

- 系统异常日志（走 logback 文件，不在本次范围）
- 日志导出功能
- 日志自动清理/归档策略

## Technical Notes

- 参考现有 entity 模式：@TableName, @TableId(AUTO), @TableField(fill=INSERT), @TableLogic
- 参考现有 controller/service 模式
- MybatisPlusMetaHandler 已有 createTime 自动填充
