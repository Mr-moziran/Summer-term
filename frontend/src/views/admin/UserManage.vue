<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <h2>用户管理</h2>
      </template>

      <!-- 筛选区 -->
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" clearable>
            <el-option label="普通用户" value="USER" />
            <el-option label="客服人员" value="AGENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已禁用" value="DISABLED" />
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
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="danger"
              link
              @click="handleStatusChange(row, 'DISABLED')"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click="handleStatusChange(row, 'ACTIVE')"
            >
              启用
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus } from '@/api/admin'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  role: '',
  status: ''
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
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'createdAt,desc'
    }
    const data = await getUsers(params)
    tableData.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  queryForm.role = ''
  queryForm.status = ''
  handleQuery()
}

async function handleStatusChange(row, newStatus) {
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${row.username} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

function getRoleLabel(role) {
  const labels = {
    USER: '普通用户',
    AGENT: '客服人员',
    ADMIN: '管理员'
  }
  return labels[role] || role
}

function getRoleTagType(role) {
  const types = {
    USER: 'info',
    AGENT: 'success',
    ADMIN: 'danger'
  }
  return types[role] || 'info'
}
</script>

<style scoped lang="scss">
.filter-form {
  margin-bottom: 20px;
}
</style>
