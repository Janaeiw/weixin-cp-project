# Frontend Quality Guidelines

## 技术栈
- Vue 3 + Vite + TypeScript + Element Plus + Tailwind CSS
- 基础模板：pure-admin-thin
- 所有依赖版本完全参照 pure-admin-thin 原始配置，不主动升级

---

## 核心约束

### 依赖版本约束

- **完全对齐 pure-admin-thin** — Vite、插件版本等均使用模板自带版本，不做升级或降级
- 如需调整版本，需有明确理由并记录在 PRD 中

---

## 代码风格

### ESLint + Prettier

- 使用 pure-admin-thin 内置的 ESLint 配置
- 保存时自动格式化（IDE 配置）
- CI 流水线中 `npm run lint` 必须通过

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `CustomerList.vue` |
| composable | `useXxx.ts` | `useCustomer.ts` |
| 普通 TS 文件 | camelCase | `auth.ts` |
| 路由路径 | 小写中划线 | `/customer/list` |
| CSS 类名 | Tailwind 为主，自定义类 kebab-case | `customer-card` |

---

## 禁止事项

1. **禁止擅自升级依赖版本** — 除非有明确理由
2. **禁止 Options API** — 必须使用 `<script setup>` Composition API
3. **禁止 `any` 类型**
4. **禁止内联样式** — 用 Tailwind 或 scoped `<style>`
5. **禁止在组件中直接调用 HTTP** — 必须通过 `src/api/` 封装
6. **禁止在前端存储敏感信息** — token 存 httpOnly cookie 或 memory（配合后端 session）
7. **禁止使用本地存储缓存业务数据** — 多实例部署下本地缓存无意义，数据从 API 获取

---

## 测试要求

| 类型 | 工具 | 覆盖范围 |
|------|------|---------|
| 单元测试 | Vitest | composables、utils 函数 |
| 组件测试 | Vitest + Vue Test Utils | 核心业务组件 |
| E2E 测试 | Cypress / Playwright（可选） | 关键业务流程 |

---

## 构建规范

- `npm run build` 必须无报错、无 TypeScript 类型错误
- 构建产物使用 Vite 默认输出到 `dist/`
- 代码分割：路由级别懒加载（pure-admin-thin 默认支持）
