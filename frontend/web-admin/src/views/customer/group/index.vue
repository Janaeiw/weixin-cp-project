<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useUserStoreHook } from "@/store/modules/user";
import { useDictStoreHook } from "@/store/modules/dict";
import { getGroupChatList, type WecomGroupChat } from "@/api/customer";
import GroupChatDetail from "./components/GroupChatDetail.vue";

defineOptions({ name: "WxCustomerGroup" });

const { wxUserId } = storeToRefs(useUserStoreHook());

// ===== 字典 =====
const dictStore = useDictStoreHook();
const statusOptions = computed(() => dictStore.getDictByCode("customer_group_status"));

const statusLabel = (val: number) => {
  const item = statusOptions.value.find(d => d.value === String(val));
  return item?.label ?? "未知";
};

const statusTagType = (val: number) => {
  const map: Record<number, "danger" | "success"> = { 0: "danger", 1: "success" };
  return map[val] || "info";
};

// ===== 搜索 =====
const searchForm = ref({
  keyword: "",
  owner: "",
  status: undefined as number | undefined
});

// ===== 表格 =====
const loading = ref(false);
const tableData = ref<WecomGroupChat[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const fetchData = async () => {
  if (!wxUserId.value) return;
  loading.value = true;
  try {
    const res = await getGroupChatList({
      userId: wxUserId.value,
      keyword: searchForm.value.keyword || undefined,
      owner: searchForm.value.owner || undefined,
      status: searchForm.value.status,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    });
    if (res.code === 0) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pageNum.value = 1;
  fetchData();
};

const handleReset = () => {
  searchForm.value = { keyword: "", owner: "", status: undefined };
  handleSearch();
};

const handleSizeChange = (val: number) => {
  pageSize.value = val;
  fetchData();
};

const handleCurrentChange = (val: number) => {
  pageNum.value = val;
  fetchData();
};

// ===== 客群详情弹窗 =====
const detailVisible = ref(false);
const currentGroupChat = ref<WecomGroupChat | null>(null);

const handleViewDetail = (row: WecomGroupChat) => {
  currentGroupChat.value = row;
  detailVisible.value = true;
};

// ===== 格式化时间戳 =====
const formatTimestamp = (timestamp: number | null | undefined) => {
  if (!timestamp) return "-";
  return new Date(timestamp * 1000).toLocaleString();
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="群名称">
          <el-input
            v-model="searchForm.keyword"
            class="w-[150px]!"
            placeholder="请输入群名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="群主">
          <el-input
            v-model="searchForm.owner"
            class="w-[130px]!"
            placeholder="请输入群主"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            class="w-[120px]!"
            placeholder="全部"
            clearable
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-medium">客群列表</span>
      </template>

      <el-empty v-if="!wxUserId" description="未绑定企微员工" />

      <template v-else>
        <el-table v-loading="loading" :data="tableData" stripe border>
          <el-table-column type="index" label="编号" width="70" align="center" />
          <el-table-column prop="name" label="群名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="owner" label="群主" width="120" />
          <el-table-column prop="memberCount" label="成员数" width="90" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="notice" label="群公告" min-width="200" show-overflow-tooltip />
          <el-table-column label="创建时间" width="170" align="center">
            <template #default="{ row }">
              {{ formatTimestamp(row.createTimeField) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
    </el-card>

    <!-- 客群详情弹窗 -->
    <GroupChatDetail
      v-model:visible="detailVisible"
      :group-chat="currentGroupChat"
    />
  </div>
</template>
