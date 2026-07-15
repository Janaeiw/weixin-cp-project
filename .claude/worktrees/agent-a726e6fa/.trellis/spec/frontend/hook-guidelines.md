# Frontend Hook Guidelines（Composables）

## 说明
Vue 3 中对应的 "Hook" 概念是 **Composable**（`useXxx` 函数）。

---

## 命名规范

- 文件名：`useXxx.ts`，如 `useCustomer.ts`、`useAuth.ts`
- 函数名与文件名一致：`export function useCustomer() { ... }`
- 放置位置：`src/composables/` 目录

---

## 标准结构

```typescript
// src/composables/useCustomer.ts
import { ref } from 'vue'
import { getCustomerList, type CustomerVO } from '@/api/customer'

export function useCustomer() {
  const loading = ref(false)
  const list = ref<CustomerVO[]>([])

  async function fetchList(params: CustomerQueryDTO) {
    loading.value = true
    try {
      const res = await getCustomerList(params)
      list.value = res.data.records
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    list,
    fetchList,
  }
}
```

---

## 使用规则

1. **Composable 封装有状态的复用逻辑** — 数据获取、表单逻辑、权限检查等
2. **在组件顶部调用**，不要在条件分支或循环中调用
3. **每个 composable 职责单一** — 一个文件对应一个业务领域
4. **返回值用对象解构**，不用数组（更灵活，顺序无关）

---

## 数据获取模式

```typescript
// composable 内部封装 loading + error + data
export function useXxx() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const data = ref<T | null>(null)

  async function fetch() {
    loading.value = true
    error.value = null
    try {
      const res = await apiCall()
      data.value = res.data
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  return { loading, error, data, fetch }
}
```

---

## 禁止事项

1. **禁止在 composable 中使用 UI 相关逻辑**（如 ElMessage） — 由调用方处理
2. **禁止在 composable 中使用全局 store** — 通过参数注入或在组件中组合
3. **禁止写过大的 composable** — 超过 100 行应拆分
