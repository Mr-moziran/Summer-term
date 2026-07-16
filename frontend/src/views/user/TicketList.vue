<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="flex-between">
          <h2>我的工单</h2>
          <div>
            <el-button @click="goToAskAi">
              <el-icon><ChatDotRound /></el-icon>
              智能助手
            </el-button>
            <el-button type="primary" @click="goToNewTicket">
              <el-icon><Plus /></el-icon>
              提交工单
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区 -->
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option
              v-for="(item, key) in TICKET_STATUS"
              :key="key"
              :label="item.label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.category" placeholder="全部" clearable>
            <el-option
              v-for="(item, key) in TICKET_CATEGORY"
              :key="key"
              :label="item.label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="工单ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="category" label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category" :color="TICKET_CATEGORY[row.category]?.color" effect="light">
              {{ TICKET_CATEGORY[row.category]?.label }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.priority" :type="TICKET_PRIORITY[row.priority]?.type">
              {{ TICKET_PRIORITY[row.priority]?.label }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="TICKET_STATUS[row.status]?.type">
              {{ TICKET_STATUS[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="mt-20"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTickets } from '@/api/ticket'
import { TICKET_STATUS, TICKET_CATEGORY, TICKET_PRIORITY } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  status: '',
  category: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      sort: 'createdAt,desc'
    }
    const data = await getTickets(params)
    tableData.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('加载工单列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  queryForm.category = ''
  handleQuery()
}

function viewDetail(id) {
  router.push(`/tickets/${id}`)
}

function goToNewTicket() {
  router.push('/tickets/new')
}

function goToAskAi() {
  router.push('/ask-ai')
}
</script>

<style scoped lang="scss">
.filter-form {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}
</style>
