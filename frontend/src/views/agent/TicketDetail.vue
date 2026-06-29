<template>
  <div class="page-container agent-ticket-detail">
    <el-row :gutter="20" v-loading="loading">
      <!-- 左侧：工单信息 -->
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>工单信息</span>
              <div class="header-tags">
                <el-tag :type="TICKET_STATUS[ticket.status]?.type">
                  {{ TICKET_STATUS[ticket.status]?.label }}
                </el-tag>
                <el-tag v-if="ticket.priority" :type="TICKET_PRIORITY[ticket.priority]?.type">
                  {{ TICKET_PRIORITY[ticket.priority]?.label }}
                </el-tag>
              </div>
            </div>
          </template>

          <el-descriptions :column="1" border v-if="ticket">
            <el-descriptions-item label="工单ID">
              {{ ticket.id }}
            </el-descriptions-item>
            <el-descriptions-item label="提交人">
              {{ ticket.submitterName }}
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag v-if="ticket.category" :color="TICKET_CATEGORY[ticket.category]?.color" effect="light">
                {{ TICKET_CATEGORY[ticket.category]?.label }}
              </el-tag>
              <span v-else class="text-muted">待AI分类</span>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatDateTime(ticket.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="标题">
              <strong>{{ ticket.title }}</strong>
            </el-descriptions-item>
            <el-descriptions-item label="描述">
              <div class="description-text">{{ ticket.description }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 历史回复 -->
          <div class="replies-section">
            <h3 class="section-title">历史回复</h3>
            <el-timeline v-if="replies.length > 0">
              <el-timeline-item
                v-for="reply in replies"
                :key="reply.id"
                :timestamp="formatDateTime(reply.createdAt)"
                placement="top"
              >
                <div class="reply-item">
                  <div class="reply-header">
                    <span class="author">{{ reply.authorName }}</span>
                    <el-tag v-if="reply.aiAdopted" size="small" type="success">AI采纳</el-tag>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无回复记录" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：AI建议 -->
      <el-col :span="12">
        <el-card class="ai-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><MagicStick /></el-icon>
                AI 回复建议
              </span>
              <el-button
                size="small"
                :loading="aiLoading"
                @click="loadAiSuggestion"
              >
                重新生成
              </el-button>
            </div>
          </template>

          <div v-if="aiSuggestion" class="ai-content">
            <div class="ai-draft">
              <h4>推荐回复</h4>
              <div class="draft-text">{{ aiSuggestion.draft }}</div>
              <el-button type="primary" size="small" @click="adoptDraft">
                采纳此草稿
              </el-button>
            </div>

            <div v-if="aiSuggestion.similarTickets?.length > 0" class="similar-tickets">
              <h4>相似历史工单</h4>
              <div
                v-for="similar in aiSuggestion.similarTickets"
                :key="similar.ticketId"
                class="similar-item"
              >
                <div class="similar-header">
                  <span class="ticket-id">#{{ similar.ticketId }}</span>
                  <el-tag size="small" type="info">
                    相似度 {{ (similar.similarity * 100).toFixed(0) }}%
                  </el-tag>
                </div>
                <div class="similar-title">{{ similar.title }}</div>
                <div class="similar-solution">
                  <strong>解决方案：</strong>{{ similar.solution }}
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="点击"加载AI建议"获取智能回复" :image-size="100" />
        </el-card>

        <!-- 回复编辑器 -->
        <el-card class="reply-card">
          <template #header>
            <span>发送回复</span>
          </template>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="8"
            placeholder="请输入回复内容..."
            maxlength="2000"
            show-word-limit
          />
          <div class="action-buttons">
            <el-checkbox v-model="aiAdopted">标记为采纳AI建议</el-checkbox>
            <div>
              <el-button @click="goBack">返回</el-button>
              <el-button
                v-if="ticket.status !== 'RESOLVED'"
                type="success"
                @click="handleResolve"
                :loading="resolveLoading"
              >
                标记为已解决
              </el-button>
              <el-button
                type="primary"
                @click="handleReply"
                :loading="replyLoading"
                :disabled="!replyContent.trim()"
              >
                发送回复
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail, getTicketReplies, replyTicket, updateTicketStatus } from '@/api/ticket'
import { getAiSuggestion } from '@/api/ai'
import { TICKET_STATUS, TICKET_CATEGORY, TICKET_PRIORITY } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const aiLoading = ref(false)
const replyLoading = ref(false)
const resolveLoading = ref(false)

const ticket = ref({})
const replies = ref([])
const aiSuggestion = ref(null)
const replyContent = ref('')
const aiAdopted = ref(false)

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

    // 自动加载AI建议
    loadAiSuggestion()
  } catch (error) {
    ElMessage.error('加载工单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function loadAiSuggestion() {
  aiLoading.value = true
  try {
    const data = await getAiSuggestion(ticket.value.id)
    aiSuggestion.value = data

    // 更新工单的分类和优先级信息（如果AI已分类）
    if (data.category) {
      ticket.value.category = data.category
      ticket.value.priority = data.priority
    }
  } catch (error) {
    ElMessage.error('获取AI建议失败')
    console.error(error)
  } finally {
    aiLoading.value = false
  }
}

function adoptDraft() {
  replyContent.value = aiSuggestion.value.draft
  aiAdopted.value = true
  ElMessage.success('已采纳AI草稿')
}

async function handleReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replyLoading.value = true
  try {
    await replyTicket(ticket.value.id, {
      content: replyContent.value,
      aiAdopted: aiAdopted.value
    })
    ElMessage.success('回复发送成功')

    // 更新工单状态为处理中
    if (ticket.value.status === 'PENDING' || ticket.value.status === 'ASSIGNED') {
      await updateTicketStatus(ticket.value.id, 'PROCESSING')
    }

    // 重新加载数据
    replyContent.value = ''
    aiAdopted.value = false
    loadData()
  } catch (error) {
    ElMessage.error('回复发送失败')
    console.error(error)
  } finally {
    replyLoading.value = false
  }
}

async function handleResolve() {
  resolveLoading.value = true
  try {
    await updateTicketStatus(ticket.value.id, 'RESOLVED')
    ElMessage.success('工单已标记为已解决')
    ticket.value.status = 'RESOLVED'
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  } finally {
    resolveLoading.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.agent-ticket-detail {
  .info-card,
  .ai-card,
  .reply-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-tags {
      display: flex;
      gap: 8px;
    }
  }

  .description-text {
    white-space: pre-wrap;
    line-height: 1.6;
  }

  .replies-section {
    margin-top: 30px;

    .section-title {
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: 600;
    }

    .reply-item {
      .reply-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        .author {
          font-weight: 600;
          color: #333;
        }
      }

      .reply-content {
        line-height: 1.6;
        color: #666;
      }
    }
  }

  .ai-content {
    .ai-draft {
      padding: 15px;
      background: #f0f9ff;
      border: 1px solid #bfdbfe;
      border-radius: 4px;
      margin-bottom: 20px;

      h4 {
        margin-bottom: 10px;
        color: #1e40af;
      }

      .draft-text {
        margin-bottom: 15px;
        line-height: 1.8;
        color: #333;
        white-space: pre-wrap;
      }
    }

    .similar-tickets {
      h4 {
        margin-bottom: 15px;
        font-size: 14px;
        font-weight: 600;
      }

      .similar-item {
        padding: 12px;
        margin-bottom: 10px;
        background: #f5f7fa;
        border-radius: 4px;

        .similar-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .ticket-id {
            font-weight: 600;
            color: #409eff;
          }
        }

        .similar-title {
          margin-bottom: 8px;
          font-size: 14px;
          font-weight: 500;
        }

        .similar-solution {
          font-size: 13px;
          line-height: 1.6;
          color: #666;

          strong {
            color: #333;
          }
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
  }
}
</style>
