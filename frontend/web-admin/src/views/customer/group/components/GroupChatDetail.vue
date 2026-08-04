<script setup lang="ts">
import { ref, watch } from "vue";
import Fullscreen from "~icons/ri/fullscreen-fill";
import ExitFullscreen from "~icons/ri/fullscreen-exit-fill";
import {
  getGroupChatMembers,
  type WecomGroupChat,
  type WecomGroupChatMember
} from "@/api/customer";

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

// ===== 成员类型转换 =====
const getMemberTypeText = (type: number) => {
  const map: Record<number, string> = { 1: "企业成员", 2: "外部联系人" };
  return map[type] || "未知";
};

const getMemberTypeTag = (type: number) => {
  const map: Record<number, "primary" | "success"> = {
    1: "primary",
    2: "success"
  };
  return map[type] || "info";
};

// ===== 入群方式转换 =====
const getJoinSceneText = (scene: number) => {
  const map: Record<number, string> = {
    1: "成员邀请",
    2: "管理员邀请",
    3: "扫描二维码"
  };
  return map[scene] || "未知";
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
        <div class="border-b border-gray-200 flex-shrink-0">
          <h2 class="text-2xl font-bold mb-4">{{ groupChat.name }}</h2>
          <br />
          <el-descriptions :column="2" direction="vertical">
            <el-descriptions-item label="群主">{{
              groupChat.owner
            }}</el-descriptions-item>
            <el-descriptions-item label="成员数">{{
              groupChat.memberCount
            }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{
              formatTimestamp(groupChat.createTimeField)
            }}</el-descriptions-item>
            <el-descriptions-item label="Chat ID">
              <span class="font-mono text-sm">{{ groupChat.chatId }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="groupChat.notice" class="mt-4">
            <span class="text-gray-500">群公告：</span>
            <div class="mt-2 p-3 bg-gray-50 rounded">
              {{ groupChat.notice }}
            </div>
          </div>
        </div>

        <br />

        <!-- 成员列表 -->
        <div class="flex-1 overflow-auto">
          <h3 class="text-lg font-medium mb-4">成员列表</h3>
          <el-table v-loading="membersLoading" :data="members" border stripe>
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="groupNickname" label="群昵称" width="120" />
            <el-table-column
              prop="userId"
              label="UserID"
              width="150"
              show-overflow-tooltip
            />
            <el-table-column label="成员类型" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getMemberTypeTag(row.memberType)" size="small">
                  {{ getMemberTypeText(row.memberType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="入群方式" width="120">
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
