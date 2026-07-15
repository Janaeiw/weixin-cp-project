# Frontend Component Guidelines

## Tech Stack
- Vue 3 Composition API + `<script setup>` + TypeScript

---

## 组件结构（标准顺序）

```vue
<script setup lang="ts">
// 1. defineProps / defineEmits
// 2. 导入（composables、utils、子组件）
// 3. 响应式状态（ref、reactive）
// 4. 计算属性（computed）
// 5. 方法
// 6. 生命周期（onMounted 等）
// 7. watch
</script>

<template>
  <!-- 模板 -->
</template>

<style scoped>
/* 仅在需要覆盖第三方样式或复杂布局时使用 */
</style>
```

---

## Props 规范

```typescript
// ✅ 使用类型声明 + withDefaults
const props = withDefaults(defineProps<{
  customerId: string
  name?: string
  status?: 'active' | 'inactive'
}>(), {
  status: 'active'
})

// ✅ 使用 defineEmits
const emit = defineEmits<{
  (e: 'update', id: string): void
  (e: 'delete', id: string): void
}>()
```

---

## 样式规范

- **优先使用 Tailwind CSS**（pure-admin-thin 内置）
- **scoped 必须加** — 所有 `<style>` 块必须 `<style scoped>`
- **禁止内联样式** — 除非动态计算的值（`:style`）
- **深选择器** — 覆盖第三方组件样式用 `:deep(.class)`

---

## 组件复用规则

1. **页面私有组件** → 放 `views/xxx/components/` 下
2. **跨页面复用** → 抽到 `src/components/`
3. **第三方组件二次封装** → 统一放 `src/components/`，如 `ReIcon`、`ReTable`
4. **单文件组件不超过 300 行** — 超出则拆分子组件

---

## 禁止事项

1. **禁止 Options API** — 必须使用 `<script setup>` Composition API
2. **禁止在模板中写复杂逻辑** — 提取到 computed 或 method
3. **禁止组件内直接发请求** — 使用 composables 封装数据获取
4. **禁止 `any` 类型** — props 和 emits 必须有明确类型

---

## 富文本编辑器（WangEditor）

项目使用 `@wangeditor/editor-for-vue` v5 作为富文本编辑器。

### 基本用法

```vue
<script setup lang="ts">
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import "@wangeditor/editor/dist/css/style.css";
import type { IDomEditor, IEditorConfig } from "@wangeditor/editor";

const editorRef = ref<IDomEditor | null>(null);

const toolbarConfig = {};
const editorConfig: Partial<IEditorConfig> = {
  placeholder: "请输入内容...",
  MENU_CONF: {
    uploadImage: {
      customUpload(file: File, insertFn: (url: string) => void) {
        // 调用 uploadImage API，成功后 insertFn(url)
      }
    }
  }
};

// 必须在组件销毁时销毁编辑器
onBeforeUnmount(() => {
  editorRef.value?.destroy();
});
</script>

<template>
  <div>
    <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" />
    <Editor v-model="content" :defaultConfig="editorConfig" @onCreated="onEditorCreated" />
  </div>
</template>
```

### 注意事项

- 编辑器必须在 `onBeforeUnmount` 中调用 `destroy()`，否则内存泄漏
- 图片上传复用 `/api/library/image/upload` 接口
- Toolbar 和 Editor 需要分别引入
