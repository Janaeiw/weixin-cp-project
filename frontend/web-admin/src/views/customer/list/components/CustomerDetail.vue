<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useDictStoreHook } from "@/store/modules/dict";
import Fullscreen from "~icons/ri/fullscreen-fill";
import ExitFullscreen from "~icons/ri/fullscreen-exit-fill";
import {
  getCustomerFollows,
  type WecomCustomer,
  type WecomCustomerFollow
} from "@/api/customer";

const props = defineProps<{
  visible: boolean;
  customer: WecomCustomer | null;
}>();

const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
}>();

// ===== 全屏控制 =====
const isFullscreen = ref(false);
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
};

// ===== 字典 =====
const dictStore = useDictStoreHook();
const genderOptions = computed(() =>
  dictStore.getDictByCode("customer_gender")
);
const typeOptions = computed(() => dictStore.getDictByCode("customer_type"));

const genderLabel = (val: number) => {
  const item = genderOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

const typeLabel = (val: number) => {
  const item = typeOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

const genderTagType = (val: number) => {
  const map: Record<number, "primary" | "success" | "warning" | "info"> = {
    1: "primary",
    2: "success"
  };
  return map[val] || "info";
};

const typeTagType = (val: number) => {
  const map: Record<number, "primary" | "success"> = {
    1: "success",
    2: "primary"
  };
  return map[val] || "info";
};

// ===== 跟进人列表 =====
const followsLoading = ref(false);
const follows = ref<WecomCustomerFollow[]>([]);

const fetchFollows = async () => {
  if (!props.customer?.externalUserid) return;

  followsLoading.value = true;
  try {
    const res = await getCustomerFollows(props.customer.externalUserid);
    if (res.code === 0) {
      follows.value = res.data;
    }
  } finally {
    followsLoading.value = false;
  }
};

watch(
  () => props.visible,
  val => {
    if (val && props.customer) {
      isFullscreen.value = false;
      fetchFollows();
    }
  }
);

// ===== 关闭弹窗 =====
const handleClose = () => {
  emit("update:visible", false);
};

// ===== 添加方式转换 =====
const getAddWayText = (addWay: string) => {
  const map: Record<string, string> = {
    "0": "未定义",
    "1": "手动添加",
    "2": "外部联系人分享",
    "3": "群聊",
    "4": "名片分享",
    "5": "扫描二维码",
    "6": "搜索手机号",
    "7": "搜索邮箱",
    "8": "来自微信",
    "9": "来自腾讯会议",
    "10": "来自视频号"
  };
  return map[addWay] || addWay;
};

// ===== 标签类型转换 =====
const getTagTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: "企业设置",
    2: "用户自定义",
    3: "规则组标签"
  };
  return map[type] || "未知";
};

// ===== 视频号场景转换 =====
const getChannelSourceText = (source: number) => {
  const map: Record<number, string> = {
    0: "未知",
    1: "视频号主页",
    2: "视频号直播间",
    3: "视频号留资服务"
  };
  return map[source] || "未知";
};

// ===== 解析 JSON =====
const parseJson = (jsonStr: string | null | undefined) => {
  if (!jsonStr) return [];
  try {
    return JSON.parse(jsonStr);
  } catch {
    return [];
  }
};
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="客户详情"
    :width="isFullscreen ? '100%' : '900px'"
    :fullscreen="isFullscreen"
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <template #header>
      <div class="flex items-center justify-between w-full">
        <span class="text-lg font-medium">客户详情</span>
        <IconifyIconOffline
          class="cursor-pointer"
          :icon="isFullscreen ? ExitFullscreen : Fullscreen"
          @click="toggleFullscreen"
        />
      </div>
    </template>

    <template v-if="customer">
      <div
        class="flex flex-col"
        :style="
          isFullscreen ? 'height: calc(100vh - 120px)' : 'max-height: 70vh'
        "
      >
        <!-- 基本信息 -->
        <div class="flex-shrink-0">
          <div class="flex items-start gap-6">
            <el-avatar :src="customer.avatar" :size="60">
              {{ customer.name?.charAt(0) }}
            </el-avatar>
            <div class="flex-1">
              <h2 class="text-2xl font-bold mb-4">{{ customer.name }}</h2>
              <br />
              <el-descriptions :column="3" direction="vertical">
                <el-descriptions-item label="客户ID">{{
                  customer.id
                }}</el-descriptions-item>
                <el-descriptions-item label="External UserID">
                  <span class="font-mono text-sm">{{
                    customer.externalUserid
                  }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="性别">
                  <el-tag :type="genderTagType(customer.gender)" size="small">
                    {{ genderLabel(customer.gender) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="类型">
                  <el-tag :type="typeTagType(customer.type)" size="small">
                    {{ typeLabel(customer.type) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="职位">{{
                  customer.position || "-"
                }}</el-descriptions-item>
                <el-descriptions-item label="所属企业">{{
                  customer.corpName || "-"
                }}</el-descriptions-item>
                <el-descriptions-item label="企业全称">{{
                  customer.corpFullName || "-"
                }}</el-descriptions-item>
                <el-descriptions-item label="UnionID">
                  <span class="font-mono text-sm">{{
                    customer.unionId || "-"
                  }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{
                  customer.createTime
                }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{
                  customer.updateTime
                }}</el-descriptions-item>
              </el-descriptions>
              <div v-if="customer.externalProfile" class="mt-4">
                <span class="text-gray-500">扩展属性：</span>
                <span class="text-sm">{{ customer.externalProfile }}</span>
              </div>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 跟进人列表 -->
        <div class="flex-1 overflow-auto">
          <div v-loading="followsLoading">
            <template v-if="follows.length">
              <el-card
                v-for="follow in follows"
                :key="follow.id"
                class="mb-4"
                shadow="hover"
              >
                <template #header>
                  <div class="flex items-center justify-between">
                    <span class="font-medium">跟进人：{{ follow.userid }}</span>
                    <el-tag size="small">ID: {{ follow.id }}</el-tag>
                  </div>
                </template>
                <el-descriptions :column="3" size="small" direction="vertical">
                  <el-descriptions-item label="备注">{{
                    follow.remark || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="描述">{{
                    follow.description || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="添加方式">{{
                    getAddWayText(follow.addWay)
                  }}</el-descriptions-item>
                  <el-descriptions-item label="状态标签">{{
                    follow.state || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="企业备注">{{
                    follow.remarkCompany || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="企业名称备注">{{
                    follow.remarkCorpName || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="手机号备注">
                    {{ parseJson(follow.remarkMobiles).join(", ") || "-" }}
                  </el-descriptions-item>
                  <el-descriptions-item label="操作人">{{
                    follow.operatorUserid || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="添加时间">{{
                    follow.followCreateTime ?? "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="记录创建时间">{{
                    follow.createTime || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="标签" :span="3">
                    <template v-if="parseJson(follow.tags).length">
                      <el-tag
                        v-for="(tag, idx) in parseJson(follow.tags)"
                        :key="idx"
                        size="small"
                        class="mr-1 mb-1"
                      >
                        {{ tag.tagName }}
                        <span v-if="tag.groupName" class="text-gray-400 ml-1"
                          >({{ tag.groupName }})</span
                        >
                        <span class="text-gray-400 ml-1"
                          >[{{ getTagTypeText(tag.type) }}]</span
                        >
                      </el-tag>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="标签ID" :span="3">
                    {{ parseJson(follow.tagIds).join(", ") || "-" }}
                  </el-descriptions-item>
                  <el-descriptions-item label="视频号">
                    <template v-if="follow.wechatChannelsNickname">
                      <div>{{ follow.wechatChannelsNickname }}</div>
                      <div class="text-xs text-gray-400">
                        {{ getChannelSourceText(follow.wechatChannelsSource) }}
                      </div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </template>
            <el-empty v-else description="暂无跟进人数据" />
          </div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>
