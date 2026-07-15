# Frontend Type Safety

## 方案
TypeScript（严格模式），pure-admin-thin 默认支持

---

## tsconfig 关键配置

```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "esModuleInterop": true,
    "moduleResolution": "bundler",
    "jsx": "preserve",
    "types": ["vite/client"]
  }
}
```

---

## 类型组织

| 类型 | 放置位置 | 示例 |
|------|---------|------|
| 全局公共类型 | `src/types/` 目录 | `UserVO`, `PageResult<T>` |
| API 相关类型 | 对应 `src/api/xxx.ts` 文件顶部或同目录 `types.ts` | `CustomerVO`, `CustomerQueryDTO` |
| 组件 Props 类型 | 组件内 `defineProps<{...}>()` | — |
| Store 状态类型 | store 文件内定义 | — |

---

## API 类型规范

```typescript
// src/api/customer.ts
// 请求参数类型
export interface CustomerQueryDTO {
  keyword?: string
  status?: 'active' | 'inactive'
  pageNum?: number
  pageSize?: number
}

// 响应数据类型
export interface CustomerVO {
  id: string
  name: string
  status: 'active' | 'inactive'
  createTime: string
}

// 接口函数
export function getCustomerList(params: CustomerQueryDTO) {
  return http.get<PageResult<CustomerVO>>('/api/customer/list', { params })
}
```

---

## 规则

1. **禁止 `any`** — 用 `unknown` + 类型守卫代替
2. **禁止 `as` 类型断言** — 除非万不得已，且必须加注释说明原因
3. **API 响应必须有类型** — 不允许 `res.data` 无类型直接使用
4. **枚举值用联合类型** — `'active' | 'inactive'` 优于 `number` 枚举（与后端约定好）
5. **泛型工具类型** — 复用 `Partial<T>`、`Pick<T>`、`Omit<T>` 等内置类型
6. **`interface` 优先于 `type`** — 用于对象结构；`type` 用于联合类型、工具类型

---

## 运行时校验

表单校验使用 Element Plus 内置的表单校验机制，不引入额外库：

```typescript
const rules = {
  name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}
```
