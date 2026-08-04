<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useDictStoreHook } from "@/store/modules/dict";
import Fullscreen from "~icons/ri/fullscreen-fill";
import ExitFullscreen from "~icons/ri/fullscreen-exit-fill";
import {
  getGroupChatMembers,
  type WecomGroupChat,
  type WecomGroupChatMember
} from "@/api/customer";

// ===== 字典 =====
const dictStore = useDictStoreHook();
const statusOptions = computed(() => dictStore.getDictByCode("customer_group_status"));
const memberTypeOptions = computed(() => dictStore.getDictByCode("customer_group_member_type"));
const joinSceneOptions = computed(() => dictStore.getDictByCode("customer_group_member_join_scene"));

const props = defineProps<{
  visible: boolean;
  groupChat: WecomGroupChat | null;
}>();

const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
}>();

// ===== 全屏控制 =====
const isFullscreen = ref(false);
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
};

// ===== 成员列表 =====
const membersLoading = ref(false);
const members = ref<WecomGroupChatMember[]>([]);

const fetchMembers = async () => {
  if (!props.groupChat?.chatId) return;

  membersLoading.value = true;
  try {
    const res = await getGroupChatMembers(props.groupChat.chatId);
    if (res.code === 0) {
      members.value = res.data;
    }
  } finally {
    membersLoading.value = false;
  }
};

watch(
  () => props.visible,
  val => {
    if (val && props.groupChat) {
      isFullscreen.value = false;
      fetchMembers();
    }
  }
);

// ===== 关闭弹窗 =====
const handleClose = () => {
  emit("update:visible", false);
};

// ===== 状态转换 =====
const getStatusText = (val: number) => {
  const item = statusOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

const getStatusTagType = (val: number) => {
  const map: Record<number, "danger" | "success"> = { 0: "danger", 1: "success" };
  return map[val] || "info";
};

// ===== 成员类型转换 =====
const getMemberTypeText = (val: number) => {
  const item = memberTypeOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

const getMemberTypeTag = (val: number) => {
  const map: Record<number, "primary" | "success"> = {
    1: "primary",
    2: "success"
  };
  return map[val] || "info";
};

// ===== 入群方式转换 =====
const getJoinSceneText = (val: number) => {
  const item = joinSceneOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

// ===== 格式化时间戳 =====
const formatTimestamp = (timestamp: number | null | undefined) => {
  if (!timestamp) return "-";
  return new Date(timestamp * 1000).toLocaleString();
};
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="客群详情"
    :width="isFullscreen ? '100%' : '900px'"
    :fullscreen="isFullscreen"
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <template #header>
      <div class="flex items-center justify-between w-full">
        <span class="text-lg font-medium">客群详情</span>
        <IconifyIconOffline
          class="cursor-pointer"
          :icon="isFullscreen ? ExitFullscreen : Fullscreen"
          @click="toggleFullscreen"
        />
      </div>
    </template>

    <template v-if="groupChat">
      <div
        class="flex flex-col"
        :style="
          isFullscreen ? 'height: calc(100vh - 120px)' : 'max-height: 70vh'
        "
      >
        <!-- 基本信息 -->
        <div class="flex-shrink-0">
          <h2 class="text-2xl font-bold mb-4">{{ groupChat.name }}</h2>
          <el-descriptions :column="3" direction="vertical" border>
            <el-descriptions-item label="群聊ID">
              <span class="font-mono text-sm">{{ groupChat.chatId }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="群主">{{ groupChat.owner }}</el-descriptions-item>
            <el-descriptions-item label="成员数">{{ groupChat.memberCount }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(groupChat.status)" size="small">
                {{ getStatusText(groupChat.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatTimestamp(groupChat.createTimeField) }}
            </el-descriptions-item>
            <el-descriptions-item label="记录创建时间">
              {{ groupChat.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="记录更新时间">
              {{ groupChat.updateTime }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 群公告 -->
          <div v-if="groupChat.notice" class="mt-4">
            <div class="flex items-center gap-2 mb-2">
              <el-icon class="text-orange-500"><i class="ri-megaphone-line" /></el-icon>
              <span class="font-medium text-gray-700">群公告</span>
            </div>
            <el-card shadow="never" class="bg-orange-50 border-orange-200">
              <div class="text-gray-700 whitespace-pre-wrap leading-relaxed">
                {{ groupChat.notice }}
              </div>
            </el-card>
          </div>
        </div>

        <el-divider />

        <!-- 成员列表 -->
        <div class="flex-1 overflow-auto">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium">成员列表</h3>
            <el-tag type="info" size="small">共 {{ members.length }} 人</el-tag>
          </div>
          <el-table v-loading="membersLoading" :data="members" border stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="groupNickname" label="群昵称" width="120" show-overflow-tooltip />
            <el-table-column prop="userId" label="UserID" width="140" show-overflow-tooltip />
            <el-table-column label="成员类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getMemberTypeTag(row.memberType)" size="small">
                  {{ getMemberTypeText(row.memberType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="入群方式" width="100">
              <template #default="{ row }">
                {{ getJoinSceneText(row.joinScene) }}
              </template>
            </el-table-column>
            <el-table-column label="入群时间" width="170">
              <template #default="{ row }">
                {{ formatTimestamp(row.joinTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
  </el-dialog>
</template>
