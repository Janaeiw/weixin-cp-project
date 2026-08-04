<script setup lang="ts">
import { ref, watch } from "vue";
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
const formatTimestamp = (timestamp: number) => {
  if (!timestamp) return "-";
  return new Date(timestamp * 1000).toLocaleString();
};
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="客群详情"
    direction="rtl"
    size="100%"
    :before-close="handleClose"
    class="group-chat-detail-drawer"
  >
    <template v-if="groupChat">
      <div class="flex flex-col h-full">
        <!-- 基本信息 -->
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-2xl font-bold mb-4">{{ groupChat.name }}</h2>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <span class="text-gray-500">群主：</span>
              <span>{{ groupChat.owner }}</span>
            </div>
            <div>
              <span class="text-gray-500">成员数：</span>
              <span>{{ groupChat.memberCount }}</span>
            </div>
            <div>
              <span class="text-gray-500">创建时间：</span>
              <span>{{ formatTimestamp(groupChat.createTimeField) }}</span>
            </div>
            <div>
              <span class="text-gray-500">Chat ID：</span>
              <span class="font-mono text-sm">{{ groupChat.chatId }}</span>
            </div>
          </div>
          <div v-if="groupChat.notice" class="mt-4">
            <span class="text-gray-500">群公告：</span>
            <div class="mt-2 p-3 bg-gray-50 rounded">
              {{ groupChat.notice }}
            </div>
          </div>
        </div>

        <!-- 成员列表 -->
        <div class="flex-1 p-6 overflow-auto">
          <h3 class="text-lg font-medium mb-4">成员列表</h3>
          <el-table v-loading="membersLoading" :data="members" border>
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
                <el-tag :type="getMemberTypeTag(row.memberType)">
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
  </el-drawer>
</template>

<style scoped>
.group-chat-detail-drawer :deep(.el-drawer__body) {
  padding: 0;
}
</style>
