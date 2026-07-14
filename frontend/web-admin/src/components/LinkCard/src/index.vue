<script setup lang="ts">
defineOptions({ name: "LinkCard" });

defineProps<{
  image?: string;
  title?: string;
  description?: string;
}>();
</script>

<template>
  <div class="link-card">
    <!-- 顶部：标题 -->
    <div v-if="title" class="link-card__title" :title="title">
      {{ title }}
    </div>

    <!-- 底部：描述 + 图片 -->
    <div class="link-card__footer">
      <div v-if="description" class="link-card__desc" :title="description">
        {{ description }}
      </div>
      <div v-else class="link-card__desc" />
      <div v-if="image" class="link-card__img">
        <el-image :src="image" fit="cover" class="w-full h-full">
          <template #error>
            <div
              class="flex items-center justify-center w-full h-full bg-gray-50 text-gray-300 text-xs"
            >
              加载失败
            </div>
          </template>
        </el-image>
      </div>
    </div>
  </div>
</template>

<style scoped>
.link-card {
  width: 250px;
  padding: 12px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgb(0 0 0 / 8%);
}

.link-card__title {
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 2;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.31;
  color: #1a1a1a;
  word-break: break-all;
  -webkit-box-orient: vertical;
}

.link-card__footer {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-top: 4px;
}

.link-card__desc {
  display: -webkit-box;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 3;
  font-size: 13px;
  line-height: 1.31;
  color: #999;
  word-break: break-all;
  -webkit-box-orient: vertical;
}

.link-card__img {
  flex-shrink: 0;
  width: 51px;
  height: 51px;
  overflow: hidden;
  border-radius: 4px;
}
</style>
