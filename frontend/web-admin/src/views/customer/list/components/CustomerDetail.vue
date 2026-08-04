<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useDictStoreHook } from "@/store/modules/dict";
import { getCustomerFollows, type WecomCustomer, type WecomCustomerFollow } from "@/api/customer";

const props = defineProps<{
  visible: boolean;
  customer: WecomCustomer | null;
}>();

const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
}>();

// ===== 字典 =====
const dictStore = useDictStoreHook();
const genderOptions = computed(() => dictStore.getDictByCode("customer_gender"));
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
  const map: Record<number, "primary" | "success" | "warning" | "info"> = { 1: "primary", 2: "success" };
  return map[val] || "info";
};

const typeTagType = (val: number) => {
  const map: Record<number, "primary" | "success"> = { 1: "success", 2: "primary" };
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

watch(() => props.visible, (val) => {
  if (val && props.customer) {
    fetchFollows();
  }
});

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

// ===== 视频号场景转换 =====
const getChannelSourceText = (source: number) => {
  const map: Record<number, string> = { 0: "未知", 1: "视频号主页", 2: "视频号直播间", 3: "视频号留资服务" };
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
  <el-drawer
    :model-value="visible"
    title="客户详情"
    direction="rtl"
    size="100%"
    :before-close="handleClose"
    class="customer-detail-drawer"
  >
    <template v-if="customer">
      <div class="flex flex-col h-full">
        <!-- 基本信息 -->
        <div class="p-6 border-b border-gray-200">
          <div class="flex items-start gap-6">
            <el-avatar :src="customer.avatar" :size="80">
              {{ customer.name?.charAt(0) }}
            </el-avatar>
            <div class="flex-1">
              <h2 class="text-2xl font-bold mb-4">{{ customer.name }}</h2>
              <div class="grid grid-cols-3 gap-4">
                <div>
                  <span class="text-gray-500">客户ID：</span>
                  <span class="font-mono text-sm">{{ customer.id }}</span>
                </div>
                <div>
                  <span class="text-gray-500">External UserID：</span>
                  <span class="font-mono text-sm">{{ customer.externalUserid }}</span>
                </div>
                <div>
                  <span class="text-gray-500">性别：</span>
                  <el-tag :type="genderTagType(customer.gender)" size="small">
                    {{ genderLabel(customer.gender) }}
                  </el-tag>
                </div>
                <div>
                  <span class="text-gray-500">类型：</span>
                  <el-tag :type="typeTagType(customer.type)" size="small">
                    {{ typeLabel(customer.type) }}
                  </el-tag>
                </div>
                <div>
                  <span class="text-gray-500">职位：</span>
                  <span>{{ customer.position || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">所属企业：</span>
                  <span>{{ customer.corpName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">企业全称：</span>
                  <span>{{ customer.corpFullName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">UnionID：</span>
                  <span class="font-mono text-sm">{{ customer.unionId || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">创建时间：</span>
                  <span>{{ customer.createTime }}</span>
                </div>
                <div>
                  <span class="text-gray-500">更新时间：</span>
                  <span>{{ customer.updateTime }}</span>
                </div>
              </div>
              <div v-if="customer.externalProfile" class="mt-4">
                <span class="text-gray-500">扩展属性：</span>
                <span class="text-sm">{{ customer.externalProfile }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 跟进人列表 -->
        <div class="flex-1 p-6 overflow-auto">
          <h3 class="text-lg font-medium mb-4">跟进人列表</h3>
          <el-table :data="follows" v-loading="followsLoading" border>
            <el-table-column prop="userid" label="员工UserID" width="130" />
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            <el-table-column label="添加方式" width="120">
              <template #default="{ row }">
                {{ getAddWayText(row.addWay) }}
              </template>
            </el-table-column>
            <el-table-column prop="state" label="状态标签" width="120" show-overflow-tooltip />
            <el-table-column prop="remarkCompany" label="企业备注" width="120" show-overflow-tooltip />
            <el-table-column prop="remarkCorpName" label="企业名称备注" width="130" show-overflow-tooltip />
            <el-table-column label="手机号备注" width="130" show-overflow-tooltip>
              <template #default="{ row }">
                {{ parseJson(row.remarkMobiles).join(', ') || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="标签" min-width="200">
              <template #default="{ row }">
                <template v-if="parseJson(row.tags).length">
                  <el-tag
                    v-for="(tag, idx) in parseJson(row.tags)"
                    :key="idx"
                    size="small"
                    class="mr-1 mb-1"
                  >
                    {{ tag.tagName }}
                    <span v-if="tag.groupName" class="text-gray-400 ml-1">({{ tag.groupName }})</span>
                  </el-tag>
                </template>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="视频号" width="150" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.wechatChannelsNickname">
                  <div>{{ row.wechatChannelsNickname }}</div>
                  <div class="text-xs text-gray-400">{{ getChannelSourceText(row.wechatChannelsSource) }}</div>
                </template>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorUserid" label="操作人" width="120" />
            <el-table-column label="添加时间" width="170">
              <template #default="{ row }">
                {{ row.followCreateTime ? new Date(row.followCreateTime * 1000).toLocaleString() : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped>
.customer-detail-drawer :deep(.el-drawer__body) {
  padding: 0;
}
</style>
