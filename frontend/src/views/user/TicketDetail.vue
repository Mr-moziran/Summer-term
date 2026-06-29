<template>
  <div class="page-container">
    <el-card v-loading="loading" class="content-card">
      <template #header>
        <div class="flex-between">
          <h2>工单详情</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <div v-if="ticket" class="ticket-detail">
        <!-- 工单基本信息 -->
        <div class="info-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工单ID">
              {{ ticket.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="TICKET_STATUS[ticket.status]?.type">
                {{ TICKET_STATUS[ticket.status]?.label }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag v-if="ticket.category" :color="TICKET_CATEGORY[ticket.category]?.color" effect="light">
                {{ TICKET_CATEGORY[ticket.category]?.label }}
              </el-tag>
              <span v-else>待分类</span>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag v-if="ticket.priority" :type="TICKET_PRIORITY[ticket.priority]?.type">
                {{ TICKET_PRIORITY[ticket.priority]?.label }}
              </el-tag>
              <span v-else>待评估</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(ticket.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(ticket.updatedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">
              {{ ticket.title }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              <div class="description-text">{{ ticket.description }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 回复记录 -->
        <div class="reply-section">
          <h3 class="section-title">处理记录</h3>
          <el-timeline v-if="replies.length > 0">
            <el-timeline-item
              v-for="reply in replies"
              :key="reply.id"
              :timestamp="formatDateTime(reply.createdAt)"
              placement="top"
            >
              <el-card>
                <div class="reply-header">
                  <span class="author-name">{{ reply.authorName }}</span>
                  <el-tag v-if="reply.isAiDraft" size="small" type="info">AI草稿</el-tag>
                  <el-tag v-if="reply.aiAdopted" size="small" type="success">AI采纳</el-tag>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无处理记录" />
        </div>

        <!-- 评价区域 (仅在工单关闭后显示) -->
        <div v-if="ticket.status === 'CLOSED' && !ticket.rating" class="rating-section">
          <h3 class="section-title">评价工单</h3>
          <el-form :model="ratingForm" label-width="80px">
            <el-form-item label="满意度">
              <el-rate v-model="ratingForm.rating" />
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input
                v-model="ratingForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入您的评价（可选）"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRate">提交评价</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 已评价显示 -->
        <div v-else-if="ticket.rating" class="rating-display">
          <h3 class="section-title">我的评价</h3>
          <div>
            <el-rate v-model="ticket.rating" disabled />
            <div v-if="ticket.ratingComment" class="rating-comment">
              {{ ticket.ratingComment }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail, getTicketReplies, rateTicket } from '@/api/ticket'
import { TICKET_STATUS, TICKET_CATEGORY, TICKET_PRIORITY } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const ticket = ref(null)
const replies = ref([])

const ratingForm = reactive({
  rating: 0,
  comment: ''
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const ticketId = route.params.id
    const [ticketData, repliesData] = await Promise.all([
      getTicketDetail(ticketId),
      getTicketReplies(ticketId)
    ])
    ticket.value = ticketData
    replies.value = repliesData || []
  } catch (error) {
    ElMessage.error('加载工单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleRate() {
  if (ratingForm.rating === 0) {
    ElMessage.warning('请先选择满意度')
    return
  }

  try {
    await rateTicket(ticket.value.id, ratingForm)
    ElMessage.success('评价提交成功')
    loadData()
  } catch (error) {
    ElMessage.error('评价提交失败')
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.ticket-detail {
  .info-section,
  .reply-section,
  .rating-section,
  .rating-display {
    margin-bottom: 30px;

    .section-title {
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .description-text {
    white-space: pre-wrap;
    line-height: 1.6;
  }

  .reply-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .author-name {
      font-weight: 600;
      color: #333;
    }
  }

  .reply-content {
    line-height: 1.6;
    color: #666;
  }

  .rating-comment {
    margin-top: 10px;
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
    line-height: 1.6;
  }
}
</style>
