<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import VueJsonPretty from "vue-json-pretty";
import "vue-json-pretty/lib/styles.css";
import { getOperationLogPage, type OperationLogItem } from "@/api/system/log";

defineOptions({ name: "SystemLog" });

// ===== 搜索 =====
const searchForm = reactive({
  module: "",
  operation: "",
  operatorName: ""
});

// ===== 表格 =====
const loading = ref(false);
const tableData = ref<OperationLogItem[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getOperationLogPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      module: searchForm.module || undefined,
      operation: searchForm.operation || undefined,
      operatorName: searchForm.operatorName || undefined
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
  searchForm.module = "";
  searchForm.operation = "";
  searchForm.operatorName = "";
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

// ===== 详情弹窗 =====
const detailVisible = ref(false);
const detailRow = ref<OperationLogItem | null>(null);

const handleDetail = (row: OperationLogItem) => {
  detailRow.value = row;
  detailVisible.value = true;
};

/** 耗时颜色 */
const costTimeType = (ms: number) => {
  if (ms > 3000) return "danger";
  if (ms > 1000) return "warning";
  return "success";
};

/** 安全解析 JSON，失败返回 null */
const safeParseJson = (str: string | undefined | null) => {
  if (!str) return null;
  try {
    return JSON.parse(str);
  } catch {
    return null;
  }
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
        <el-form-item label="所属模块">
          <el-input
            v-model="searchForm.module"
            class="w-[180px]!"
            placeholder="请输入所属模块"
            clearable
          />
        </el-form-item>
        <el-form-item label="操作概要">
          <el-input
            v-model="searchForm.operation"
            class="w-[180px]!"
            placeholder="请输入操作概要"
            clearable
          />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.operatorName"
            class="w-[180px]!"
            placeholder="请输入操作人"
            clearable
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
        <div class="flex items-center justify-between">
          <span class="font-medium">日志管理</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" stripe border>
        <el-table-column type="index" label="编号" width="70" align="center" />
        <el-table-column prop="module" label="所属模块" min-width="120" />
        <el-table-column prop="operation" label="操作概要" min-width="100" />
        <el-table-column
          prop="operatorName"
          label="操作人"
          min-width="100"
          align="center"
        />
        <el-table-column prop="requestUrl" label="请求接口" min-width="200" />
        <el-table-column
          prop="requestMethod"
          label="请求方法"
          width="100"
          align="center"
        />
        <el-table-column prop="ip" label="IP地址" min-width="130" />
        <el-table-column prop="os" label="操作系统" min-width="100" />
        <el-table-column prop="browser" label="浏览器类型" min-width="100" />
        <el-table-column
          prop="statusCode"
          label="请求状态"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.statusCode === 200 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="costTime"
          label="请求耗时"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="costTimeType(row.costTime)" size="small">
              {{ row.costTime }}ms
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="请求时间"
          min-width="160"
          align="center"
        />
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属模块">
            {{ detailRow.module }}
          </el-descriptions-item>
          <el-descriptions-item label="操作概要">
            {{ detailRow.operation }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ detailRow.operatorName }}
          </el-descriptions-item>
          <el-descriptions-item label="请求时间">
            {{ detailRow.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            {{ detailRow.requestMethod }}
          </el-descriptions-item>
          <el-descriptions-item label="请求接口">
            {{ detailRow.requestUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ detailRow.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ detailRow.os }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器类型">
            {{ detailRow.browser }}
          </el-descriptions-item>
          <el-descriptions-item label="TraceId">
            {{ detailRow.traceId }}
          </el-descriptions-item>
          <el-descriptions-item label="请求状态">
            <el-tag
              :type="detailRow.statusCode === 200 ? 'success' : 'danger'"
              size="small"
            >
              {{ detailRow.statusCode }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求耗时">
            <el-tag :type="costTimeType(detailRow.costTime)" size="small">
              {{ detailRow.costTime }}ms
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-tabs class="mt-4">
          <el-tab-pane label="请求">
            <div class="mb-2 font-medium">请求头</div>
            <div style="height: 250px; overflow: auto">
              <vue-json-pretty
                v-if="safeParseJson(detailRow.requestHeaders)"
                :data="safeParseJson(detailRow.requestHeaders)"
                :deep="2"
                :show-length="true"
              />
              <el-input
                v-else
                type="textarea"
                :model-value="detailRow.requestHeaders"
                :rows="3"
                readonly
              />
            </div>
            <div class="mt-3 mb-2 font-medium">请求体</div>
            <div style="height: 250px; overflow: auto">
              <vue-json-pretty
                v-if="safeParseJson(detailRow.requestBody)"
                :data="safeParseJson(detailRow.requestBody)"
                :deep="3"
                :show-length="true"
              />
              <el-input
                v-else
                type="textarea"
                :model-value="detailRow.requestBody"
                :rows="3"
                readonly
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="响应">
            <div class="mb-2 font-medium">响应头</div>
            <div style="height: 250px; overflow: auto">
              <vue-json-pretty
                v-if="safeParseJson(detailRow.responseHeaders)"
                :data="safeParseJson(detailRow.responseHeaders)"
                :deep="2"
                :show-length="true"
              />
              <el-input
                v-else
                type="textarea"
                :model-value="detailRow.responseHeaders"
                :rows="3"
                readonly
              />
            </div>
            <div class="mt-3 mb-2 font-medium">响应体</div>
            <div style="height: 250px; overflow: auto">
              <vue-json-pretty
                v-if="safeParseJson(detailRow.responseBody)"
                :data="safeParseJson(detailRow.responseBody)"
                :deep="3"
                :show-length="true"
              />
              <el-input
                v-else
                type="textarea"
                :model-value="detailRow.responseBody"
                :rows="3"
                readonly
              />
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="detailRow.exceptionMsg" label="异常">
            <div class="mb-2 font-medium text-red-500">异常信息</div>
            <el-input
              type="textarea"
              :model-value="detailRow.exceptionMsg"
              :rows="6"
              readonly
              class="exception-textarea"
            />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.exception-textarea :deep(.el-textarea__inner) {
  color: #f56c6c;
}
</style>
