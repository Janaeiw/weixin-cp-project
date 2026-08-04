<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useUserStoreHook } from "@/store/modules/user";
import { useDictStoreHook } from "@/store/modules/dict";
import { getCustomerList, type WecomCustomer } from "@/api/customer";
import CustomerDetail from "./components/CustomerDetail.vue";

defineOptions({ name: "WxCustomer" });

const { wxUserId } = storeToRefs(useUserStoreHook());

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

// ===== 搜索 =====
const searchForm = ref({
  keyword: "",
  gender: undefined as number | undefined,
  type: undefined as number | undefined,
  corpName: ""
});

// ===== 表格 =====
const loading = ref(false);
const tableData = ref<WecomCustomer[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const fetchData = async () => {
  if (!wxUserId.value) return;
  loading.value = true;
  try {
    const res = await getCustomerList({
      userId: wxUserId.value,
      keyword: searchForm.value.keyword || undefined,
      gender: searchForm.value.gender,
      type: searchForm.value.type,
      corpName: searchForm.value.corpName || undefined,
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
  searchForm.value = {
    keyword: "",
    gender: undefined,
    type: undefined,
    corpName: ""
  };
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

// ===== 客户详情弹窗 =====
const detailVisible = ref(false);
const currentCustomer = ref<WecomCustomer | null>(null);

const handleViewDetail = (row: WecomCustomer) => {
  currentCustomer.value = row;
  detailVisible.value = true;
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
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.keyword"
            class="w-[150px]!"
            placeholder="请输入姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-select
            v-model="searchForm.gender"
            class="w-[120px]!"
            placeholder="全部"
            clearable
          >
            <el-option
              v-for="item in genderOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="searchForm.type"
            class="w-[140px]!"
            placeholder="全部"
            clearable
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属企业">
          <el-input
            v-model="searchForm.corpName"
            class="w-[150px]!"
            placeholder="请输入企业名"
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
        <span class="font-medium">客户列表</span>
      </template>

      <el-empty v-if="!wxUserId" description="未绑定企微员工" />

      <template v-else>
        <el-table v-loading="loading" :data="tableData" stripe border>
          <el-table-column
            type="index"
            label="编号"
            width="70"
            align="center"
          />
          <el-table-column label="头像" width="80" align="center">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="40">{{
                row.name?.charAt(0)
              }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="genderTagType(row.gender)" size="small">{{
                genderLabel(row.gender)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="typeTagType(row.type)" size="small">{{
                typeLabel(row.type)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="corpName"
            label="所属企业"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column
            prop="corpFullName"
            label="企业全称"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column
            prop="position"
            label="职位"
            width="120"
            show-overflow-tooltip
          />
          <el-table-column
            prop="createTime"
            label="添加时间"
            min-width="160"
            align="center"
          />
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

    <!-- 客户详情弹窗 -->
    <CustomerDetail
      v-model:visible="detailVisible"
      :customer="currentCustomer"
    />
  </div>
</template>
