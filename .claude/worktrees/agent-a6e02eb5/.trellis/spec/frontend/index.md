# Frontend Development Guidelines

> Best practices for frontend development in this project.

---

## Tech Stack
- Vue 3 + Vite + TypeScript + Element Plus + Tailwind CSS
- 基础模板：pure-admin-thin（依赖版本完全对齐模板原始配置）
- 状态管理：Pinia

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Directory Structure](./directory-structure.md) | Module organization and file layout | ✅ Done |
| [Component Guidelines](./component-guidelines.md) | Component patterns, props, composition | ✅ Done |
| [Hook Guidelines](./hook-guidelines.md) | Custom composables, data fetching patterns | ✅ Done |
| [State Management](./state-management.md) | Pinia store patterns | ✅ Done |
| [Quality Guidelines](./quality-guidelines.md) | Code standards, forbidden patterns | ✅ Done |
| [Type Safety](./type-safety.md) | TypeScript conventions, type organization | ✅ Done |

---

## Core Constraints

1. **依赖版本完全对齐 pure-admin-thin** — 不擅自升级或降级
2. **`<script setup>` Composition API** — 禁止 Options API
3. **禁止 `any` 类型**
4. **参考 pure-admin-thin** — 项目结构和依赖必须对齐

---

**Language**: All documentation is written in **Chinese**（技术术语保留英文）.
