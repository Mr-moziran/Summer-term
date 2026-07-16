<template>
  <div class="page-container dashboard">
    <h2 class="page-title">数据大盘</h2>

    <!-- 数据卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="card-icon blue">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">今日工单</div>
            <div class="card-value">{{ stats.todayTotal || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="card-icon orange">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">待处理工单</div>
            <div class="card-value">{{ stats.pendingCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="card-icon green">
            <el-icon :size="32"><Timer /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">平均响应时长</div>
            <div class="card-value">{{ stats.avgResponseMinutes || 0 }} 分钟</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="card-icon purple">
            <el-icon :size="32"><MagicStick /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">AI 采纳率</div>
            <div class="card-value">{{ ((stats.aiAdoptionRate || 0) * 100).toFixed(1) }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card v-loading="loading" class="chart-card">
          <template #header>
            <h3>工单类型分布</h3>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '@/api/admin'
import { TICKET_CATEGORY } from '@/utils/constants'

const loading = ref(false)
const stats = ref({})
const categoryChartRef = ref()
let categoryChart = null

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (categoryChart) categoryChart.dispose()
  window.removeEventListener('resize', handleResize)
})

async function loadData() {
  loading.value = true
  try {
    const data = await getStats()
    stats.value = data
    renderCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderCategoryChart()
}

function renderCategoryChart() {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const distribution = stats.value.categoryDistribution || {}
  const data = Object.entries(distribution).map(([key, value]) => ({
    name: TICKET_CATEGORY[key]?.label || key,
    value: (value * 100).toFixed(1)
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
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data
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
.dashboard {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 10px;

      .card-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        border-radius: 12px;
        margin-right: 15px;

        &.blue {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.orange {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        &.green {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }

        &.purple {
          background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
          color: #333;
        }
      }

      .card-content {
        flex: 1;

        .card-title {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .card-value {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .charts-row {
    .chart-card {
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }

      .chart-container {
        width: 100%;
        height: 400px;
      }
    }
  }
}
</style>
