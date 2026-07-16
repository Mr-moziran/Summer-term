<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>客服绩效</h2>
            <p>查看客服处理量、解决量、回复量、AI 采纳率和平均响应时长。</p>
          </div>
          <div class="header-actions">
            <el-button @click="goDashboard">返回后台</el-button>
            <el-button :loading="loading" @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="agentId" label="客服ID" width="90" />
        <el-table-column prop="username" label="客服账号" min-width="150" />
        <el-table-column prop="assignedCount" label="分配工单" width="120" />
        <el-table-column prop="resolvedCount" label="已解决" width="120" />
        <el-table-column prop="replyCount" label="回复数" width="120" />
        <el-table-column label="AI 采纳率" width="140">
          <template #default="{ row }">
            {{ formatPercent(row.aiAdoptionRate) }}
          </template>
        </el-table-column>
        <el-table-column label="平均响应时长" width="160">
          <template #default="{ row }">
            {{ formatMinutes(row.avgResponseMinutes) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAgentPerformance } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getAgentPerformance()
  } catch (error) {
    console.error('加载客服绩效失败:', error)
  } finally {
    loading.value = false
  }
}

function formatPercent(value) {
  return `${((value || 0) * 100).toFixed(1)}%`
}

function formatMinutes(value) {
  return `${Number(value || 0).toFixed(1)} 分钟`
}

function goDashboard() {
  router.push('/admin/dashboard')
}
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  h2 {
    margin: 0 0 8px;
  }

  p {
    margin: 0;
    color: #606266;
  }
}

.header-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}
</style>
