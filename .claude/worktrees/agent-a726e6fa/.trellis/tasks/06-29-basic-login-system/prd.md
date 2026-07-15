# 基础后台登录系统

## Goal

从零搭建项目骨架，实现一个可运行的后台登录功能：后端提供登录接口，前端提供登录页面，登录成功后可访问一个基础首页。

## What I already know

- 技术栈：Java 17 + Spring Boot 3.x + MyBatis-Plus + Maven + MySQL（后端），Vue 3 + Vite 8 + TypeScript + pure-admin-thin（前端）
- 账号密码：zhongjunwei@cpapi.com / Password123（硬编码或数据库初始化均可）
- 单体架构，多实例部署约束（禁本地存储/缓存/Session）
- 企业微信集成（WxJava SDK）为后续任务，本次不涉及

## Assumptions (temporary)

- 这是全新项目，需要从零搭建 Spring Boot 和 Vue 3 工程
- 后端独立目录（如 `backend/`），前端独立目录（如 `frontend/`）
- 登录鉴权使用 JWT（适合无状态多实例部署）
- MySQL 数据库需要初始化用户表
- 前端基于 pure-admin-thin 脚手架，利用其内置登录页模板

## Open Questions

- ？

## Requirements (evolving)

1. 后端：Spring Boot 项目骨架 + 登录接口 `POST /api/auth/login`
2. 后端：MySQL 初始化用户表 + 插入默认账号
3. 后端：JWT token 生成与返回
4. 后端：统一响应格式 `R<T>`
5. 前端：Vue 3 + pure-admin-thin 项目骨架
6. 前端：登录页面（复用 pure-admin 登录页模板）
7. 前端：登录成功跳转到基础首页
8. 前端：请求拦截器自动携带 token

## Acceptance Criteria (evolving)

- [ ] 后端启动无报错，能访问 `/api/auth/login`
- [ ] 用 admin / Password123 登录返回 JWT token
- [ ] 前端启动无报错，显示登录页
- [ ] 前端登录成功跳转到首页（空白页即可）
- [ ] 前端请求头自动带 Authorization: Bearer <token>

## Definition of Done

- 后端 `mvn compile` 无错误
- 前端 `npm run build` 无错误
- 前后端联调可完成登录流程

## Out of Scope

- 企业微信集成（后续任务）
- 权限管理、菜单管理
- 用户注册、密码找回
- Docker/部署配置

## Technical Notes

- 项目 spec 已填充：`.trellis/spec/backend/` + `.trellis/spec/frontend/`
- pure-admin-thin GitHub: https://github.com/pure-admin/pure-admin-thin
- 多实例约束：Session 必须存 Redis，但本次 MVP 单机开发可先不配 Redis，用 JWT 无状态方案
