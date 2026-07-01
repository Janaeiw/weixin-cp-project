# Backend Development Guidelines

> Best practices for backend development in this project.

---

## Tech Stack
- Java 17, Spring Boot 3.x, MyBatis-Plus, Maven, MySQL
- 单体架构，必须支持多实例部署
- 企业微信集成：WxJava SDK

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Directory Structure](./directory-structure.md) | Module organization and file layout | ✅ Done |
| [Database Guidelines](./database-guidelines.md) | ORM patterns, queries, migrations | ✅ Done |
| [Error Handling](./error-handling.md) | Error types, handling strategies | ✅ Done |
| [Quality Guidelines](./quality-guidelines.md) | Code standards, forbidden patterns | ✅ Done |
| [Logging Guidelines](./logging-guidelines.md) | Structured logging, log levels | ✅ Done |

---

## Core Constraints

1. **禁止本地文件存储** — 使用 OSS（当前阶段临时使用数据库 LONGBLOB 存储图片，后续迁移至 OSS）
2. **禁止本地缓存** — 使用 Redis
3. **禁止本地 Session** — 使用 Spring Session + Redis
4. **所有代码必须支持多实例部署**
5. **禁止雪花算法 ID** — 使用 AUTO_INCREMENT（JS Number 精度限制）

---

**Language**: All documentation is written in **Chinese**（技术术语保留英文）.
