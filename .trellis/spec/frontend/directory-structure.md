# Frontend Directory Structure

## Tech Stack
- Vue 3 + Vite + TypeScript + Node.js
- 基础模板：pure-admin-thin（依赖版本完全对齐模板原始配置）

---

## 目录布局（基于 pure-admin-thin）

```
src/
├── api/                    # 后端接口定义（按模块分文件）
│   ├── customer.ts
│   ├── wx-message.ts
│   └── auth.ts
│
├── assets/                 # 静态资源（图片、字体等）
│
├── components/             # 公共组件（全局复用）
│   ├── ReIcon/             # 图标组件
│   └── ...
│
├── composables/            # 组合式函数（Vue 3 Composition API）
│   ├── useCustomer.ts
│   └── useAuth.ts
│
├── layout/                 # 页面布局（pure-admin 提供）
│
├── plugins/                # 插件注册
│
├── router/                 # 路由配置
│   ├── index.ts
│   └── modules/            # 按模块拆分路由
│       ├── customer.ts
│       └── wx.ts
│
├── store/                  # Pinia 状态管理
│   ├── index.ts
│   ├── modules/
│   │   ├── user.ts
│   │   └── app.ts
│   └── types.ts
│
├── utils/                  # 工具函数
│   ├── http/               # Axios 封装（pure-admin 自带）
│   ├── auth.ts             # Token 管理
│   └── validate.ts         # 校验规则
│
├── views/                  # 页面（按业务模块组织）
│   ├── customer/
│   │   ├── index.vue       # 列表页
│   │   ├── detail.vue      # 详情页
│   │   └── components/     # 页面内私有组件
│   │       └── CustomerForm.vue
│   ├── wx/
│   │   ├── message/
│   │   └── contact/
│   └── login/
│
├── App.vue
├── main.ts
└── env.d.ts                # 环境变量类型声明
```

---

## 规则

1. **纯展示组件** → `components/`（全局复用），`views/xxx/components/`（页面私有）
2. **API 接口** → 按后端模块拆分文件，每个文件 export 函数，函数名与后端接口对应
3. **路由** → 按业务模块拆分到 `router/modules/`，自动合并
4. **Composables** → 有状态逻辑的复用单元，命名 `useXxx`
5. **禁止在 `views/` 中放通用组件** — 必须抽到 `components/` 或 `views/xxx/components/`

---

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 页面组件 | PascalCase.vue | `CustomerList.vue` |
| 公共组件 | PascalCase.vue（文件夹+index.vue） | `ReIcon/index.vue` |
| Composable | `useXxx.ts` | `useCustomer.ts` |
| API 文件 | 小写中划线 | `customer.ts`, `wx-message.ts` |
| 路由文件 | 小写中划线 | `customer.ts` |
| Store 模块 | 小写 | `user.ts`, `app.ts` |
