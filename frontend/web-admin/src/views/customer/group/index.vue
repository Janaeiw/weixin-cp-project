<script setup lang="ts">
import { ref, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useUserStoreHook } from "@/store/modules/user";
import { getGroupChatList, type WecomGroupChat } from "@/api/customer";
import GroupChatDetail from "./components/GroupChatDetail.vue";

defineOptions({ name: "WxCustomerGroup" });

const { wxUserId } = storeToRefs(useUserStoreHook());

// ===== 搜索 =====
const searchForm = ref({ keyword: "" });

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
  searchForm.value.keyword = "";
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
const formatTimestamp = (timestamp: number) => {
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
            class="w-[180px]!"
            placeholder="请输入群名"
            clearable
            @keyup.enter="handleSearch"
          />
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
          <el-table-column
            prop="name"
            label="群名"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="owner" label="群主" width="120" />
          <el-table-column
            prop="memberCount"
            label="成员数"
            width="100"
            align="center"
          />
          <el-table-column label="创建时间" min-width="160" align="center">
            <template #default="{ row }">
              {{ formatTimestamp(row.createTimeField) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="100"
            fixed="right"
            align="center"
          >
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewDetail(row)"
                >详情</el-button
              >
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
