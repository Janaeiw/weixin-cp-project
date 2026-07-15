# Frontend State Management

## 方案
Pinia（Vue 3 官方推荐，pure-admin-thin 默认集成）

---

## 状态分类

| 类型 | 工具 | 示例 |
|------|------|------|
| 组件局部状态 | `ref` / `reactive` | 表单输入、弹窗开关、页面临时状态 |
| 全局应用状态 | Pinia store | 用户信息、权限、全局配置 |
| 服务端状态 | API + composable 封装 | 列表数据、详情数据（请求即获取，不缓存到全局） |
| 路由状态 | Vue Router | 当前页面、query 参数 |

---

## Store 规范

```typescript
// src/store/modules/user.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  // state
  const token = ref<string>('')
  const userInfo = ref<UserVO | null>(null)

  // getters
  const isLoggedIn = computed(() => !!token.value)

  // actions
  async function login(params: LoginDTO) {
    const res = await loginApi(params)
    token.value = res.data.token
    userInfo.value = res.data.user
  }

  function logout() {
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, isLoggedIn, login, logout }
})
```

---

## 使用规则

1. **用 Setup Store 风格** — `defineStore('id', () => { ... })`，不用 Options Store 风格
2. **每个 store 文件职责单一** — `user.ts`（用户）、`app.ts`（应用配置）、`permission.ts`（权限）
3. **禁止在 store 中直接操作 DOM 或调用 UI 组件**
4. **Store 只存需要跨组件共享的状态** — 单组件用的用 `ref` 即可

---

## 何时提升为全局 Store

- 需要在 3 个以上不相关组件中访问
- 状态需要在页面刷新后持久化（配合 `pinia-plugin-persistedstate`）
- 需要在路由守卫中使用

---

## 禁止事项

1. **禁止使用 Vuex** — 本项目使用 Pinia
2. **禁止在 store 中存大量列表数据** — 列表数据放组件/ composable，用 API 实时获取
3. **禁止 store 之间循环依赖** — 如需交互，在组件中同时调用两个 store
