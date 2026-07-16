<template>
  <div class="page-container admin-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">管理员后台</h2>
        <p class="page-subtitle">集中查看系统运行概况，并进入工单、用户、客服绩效和知识库管理。</p>
      </div>
      <el-button :loading="loading" @click="loadDashboard">刷新数据</el-button>
    </div>

    <el-row :gutter="20" class="module-row">
      <el-col
        v-for="module in modules"
        :key="module.key"
        :xs="24"
        :sm="12"
        :lg="8"
      >
        <el-card class="module-card" shadow="hover" @click="goModule(module.path)">
          <div class="module-content">
            <div :class="['module-icon', module.key]">
              <el-icon :size="28">
                <component :is="moduleIcon(module.key)" />
              </el-icon>
            </div>
            <div class="module-main">
              <h3>{{ module.name }}</h3>
              <p>{{ module.description }}</p>
            </div>
            <el-icon class="module-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <section class="section">
      <div class="section-header">
        <h3>数据大盘</h3>
        <span>核心运营指标</span>
      </div>

      <el-row :gutter="20" class="stats-cards">
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="never">
            <div class="card-icon blue">
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">今日工单</div>
              <div class="card-value">{{ stats.todayTotal || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="never">
            <div class="card-icon orange">
              <el-icon :size="30"><Clock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">待处理工单</div>
              <div class="card-value">{{ stats.pendingCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="never">
            <div class="card-icon green">
              <el-icon :size="30"><Timer /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">平均响应时长</div>
              <div class="card-value">{{ stats.avgResponseMinutes || 0 }} 分钟</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="never">
            <div class="card-icon purple">
              <el-icon :size="30"><MagicStick /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">AI 采纳率</div>
              <div class="card-value">{{ ((stats.aiAdoptionRate || 0) * 100).toFixed(1) }}%</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-loading="loading" class="chart-card" shadow="never">
        <template #header>
          <h3>工单类型分布</h3>
        </template>
        <div ref="categoryChartRef" class="chart-container"></div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  ArrowRight,
  Collection,
  Document,
  Clock,
  MagicStick,
  Monitor,
  Timer,
  Tickets,
  User
} from '@element-plus/icons-vue'
import { getDashboard } from '@/api/admin'
import { TICKET_CATEGORY } from '@/utils/constants'

const router = useRouter()
const loading = ref(false)
const stats = ref({})
const modules = ref([])
const categoryChartRef = ref()
let categoryChart = null

const fallbackModules = [
  { key: 'tickets', name: '工单管理', path: '/admin/tickets', description: '查看并分配工单' },
  { key: 'users', name: '用户管理', path: '/admin/users', description: '管理用户账号状态' },
  { key: 'agents', name: '客服绩效', path: '/admin/agents', description: '查看客服处理量和响应效率' },
  { key: 'knowledge', name: '知识库管理', path: '/admin/knowledge', description: '上传知识库文档供 AI 检索' }
]

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (categoryChart) categoryChart.dispose()
  window.removeEventListener('resize', handleResize)
})

async function loadDashboard() {
  loading.value = true
  try {
    const data = await getDashboard()
    stats.value = data.stats || {}
    modules.value = normalizeModules(data.modules)
    await nextTick()
    renderCategoryChart()
  } catch (error) {
    console.error('加载管理员面板失败:', error)
  } finally {
    loading.value = false
  }
}

function goModule(path) {
  if (!path) return
  router.push(path)
}

function moduleIcon(key) {
  const icons = {
    tickets: Tickets,
    users: User,
    agents: Monitor,
    knowledge: Collection
  }
  return icons[key] || Document
}

function normalizeModules(items) {
  const source = items?.length ? items : fallbackModules
  return source.filter(module => module.key !== 'stats')
}

function renderCategoryChart() {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const distribution = stats.value.categoryDistribution || {}
  const data = Object.entries(distribution).map(([key, value]) => ({
    name: TICKET_CATEGORY[key]?.label || key,
    value: Number((value * 100).toFixed(1))
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}%'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '工单类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}%'
        },
        data
      }
    ]
  }

  categoryChart.setOption(option)
}

function handleResize() {
  categoryChart?.resize()
}
</script>

<style scoped lang="scss">
.admin-dashboard {
  .page-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 20px;
  }

  .page-title {
    margin: 0 0 8px;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .page-subtitle {
    margin: 0;
    color: #606266;
    line-height: 1.6;
  }

  .module-row {
    margin-bottom: 24px;
  }

  .module-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: border-color 0.2s ease, transform 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      border-color: #409eff;
    }
  }

  .module-content {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .module-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 52px;
    height: 52px;
    border-radius: 12px;
    color: #fff;
    background: #409eff;

    &.tickets {
      background: #67c23a;
    }

    &.users {
      background: #e6a23c;
    }

    &.agents {
      background: #909399;
    }

    &.knowledge {
      background: #9254de;
    }
  }

  .module-main {
    flex: 1;

    h3 {
      margin: 0 0 6px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    p {
      margin: 0;
      color: #606266;
      line-height: 1.5;
    }
  }

  .module-arrow {
    color: #c0c4cc;
  }

  .section {
    margin-top: 4px;
  }

  .section-header {
    display: flex;
    align-items: baseline;
    gap: 10px;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    span {
      color: #909399;
    }
  }

  .stats-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    margin-bottom: 20px;

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 15px;
    }
  }

  .card-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 58px;
    height: 58px;
    border-radius: 12px;
    color: #fff;

    &.blue {
      background: #409eff;
    }

    &.orange {
      background: #e6a23c;
    }

    &.green {
      background: #67c23a;
    }

    &.purple {
      background: #9254de;
    }
  }

  .card-content {
    flex: 1;
  }

  .card-title {
    margin-bottom: 8px;
    font-size: 14px;
    color: #909399;
  }

  .card-value {
    font-size: 26px;
    font-weight: 600;
    color: #303133;
  }

  .chart-card {
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .chart-container {
    width: 100%;
    height: 400px;
  }
}
</style>
