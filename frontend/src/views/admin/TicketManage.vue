<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <h2>工单管理</h2>
          <el-button @click="goDashboard">返回后台</el-button>
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
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable>
            <el-option
              v-for="(item, key) in TICKET_PRIORITY"
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="submitterUsername" label="提交人" width="120" />
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
        <el-table-column prop="assigneeUsername" label="负责客服" width="120">
          <template #default="{ row }">
            <span v-if="row.assigneeUsername">{{ row.assigneeUsername }}</span>
            <span v-else class="text-muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              link
              @click="openAssignDialog(row)"
            >
              分配
            </el-button>
            <el-button type="primary" link @click="viewDetail(row.id)">
              查看
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

    <!-- 分配工单对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配工单"
      width="400px"
    >
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="工单ID">
          <span>{{ assignForm.ticketId }}</span>
        </el-form-item>
        <el-form-item label="工单标题">
          <span>{{ assignForm.title }}</span>
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择客服" style="width: 100%">
            <el-option
              v-for="agent in agentList"
              :key="agent.id"
              :label="agent.username"
              :value="agent.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="handleAssign">
          确定分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTickets, assignTicket } from '@/api/ticket'
import { getUsers } from '@/api/admin'
import { TICKET_STATUS, TICKET_CATEGORY, TICKET_PRIORITY } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const assignLoading = ref(false)
const tableData = ref([])
const agentList = ref([])
const assignDialogVisible = ref(false)

const queryForm = reactive({
  status: '',
  category: '',
  priority: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const assignForm = reactive({
  ticketId: null,
  title: '',
  assigneeId: null
})

onMounted(() => {
  loadData()
  loadAgents()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: pagination.page - 1,
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

async function loadAgents() {
  try {
    const data = await getUsers({ role: 'AGENT' })
    agentList.value = data.content || []
  } catch (error) {
    console.error('加载客服列表失败:', error)
  }
}

function handleQuery() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  queryForm.category = ''
  queryForm.priority = ''
  handleQuery()
}

function openAssignDialog(row) {
  assignForm.ticketId = row.id
  assignForm.title = row.title
  assignForm.assigneeId = null
  assignDialogVisible.value = true
}

async function handleAssign() {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择客服')
    return
  }

  assignLoading.value = true
  try {
    await assignTicket(assignForm.ticketId, assignForm.assigneeId)
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}

function viewDetail(id) {
  router.push(`/tickets/${id}`)
}

function goDashboard() {
  router.push('/admin/dashboard')
}
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  h2 {
    margin: 0;
  }
}

.filter-form {
  margin-bottom: 20px;
}

.text-muted {
  color: #909399;
}
</style>
