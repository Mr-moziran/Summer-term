<template>
  <div class="page-container ask-ai">
    <el-card class="content-card">
      <template #header>
        <div class="flex-between">
          <h2>智能助手</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 对话记录 -->
      <div class="chat-window" ref="chatWindowRef">
        <div v-if="messages.length === 0" class="empty-tip">
          <el-icon :size="48"><ChatDotRound /></el-icon>
          <p>您好，我是智能客服助手。请描述您遇到的问题，我会先尝试为您解答。</p>
          <p class="hint">如果我无法解决，可以输入“转人工”，我会为您转接人工客服。</p>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['chat-item', msg.role]"
        >
          <!-- 用户提问 -->
          <div v-if="msg.role === 'user'" class="bubble user-bubble">
            {{ msg.content }}
          </div>

          <!-- AI 回复 -->
          <div v-else class="bubble ai-bubble">
            <!-- 已转人工 -->
            <template v-if="msg.resultType === ASK_AI_RESULT_TYPE.ESCALATED">
              <el-alert type="warning" :closable="false" show-icon>
                <template #title>已为您转接人工客服</template>
                <div class="escalated-info">
                  {{ msg.answer || '即将为您转接人工客服，请保持当前页面，稍后将由人工客服继续处理。' }}
                </div>
              </el-alert>
            </template>

            <!-- 正常回答 / 带提醒回答 -->
            <template v-else>
              <div class="answer-text">{{ msg.answer }}</div>
              <el-alert
                v-if="msg.warning"
                type="info"
                :closable="false"
                class="mt-10"
              >
                {{ msg.warning }}
              </el-alert>

              <!-- 知识来源 -->
              <div v-if="msg.references?.length" class="references">
                <div class="references-title">参考资料：</div>
                <div
                  v-for="(ref, i) in msg.references"
                  :key="i"
                  class="reference-item"
                >
                  <el-icon><Document /></el-icon>
                  <span class="ref-title">{{ ref.title }}</span>
                  <el-tag size="small" type="info">
                    相似度 {{ (ref.score * 100).toFixed(0) }}%
                  </el-tag>
                </div>
              </div>

              <!-- 未解决可转人工 -->
              <div v-if="msg.canEscalate" class="escalate-action">
                <span class="escalate-hint">问题没有解决？</span>
                <el-button
                  type="warning"
                  size="small"
                  link
                  @click="escalateToHuman"
                >
                  转人工客服
                </el-button>
              </div>
            </template>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="chat-item ai">
          <div class="bubble ai-bubble">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在思考...
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="question"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          :disabled="loading || humanHandoffPending"
          :placeholder="humanHandoffPending ? '人工客服接管中，请保持当前页面等待' : '请输入您的问题，按 Enter 发送（Shift+Enter 换行）'"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-buttons">
          <el-button
            type="primary"
            :loading="loading"
            :disabled="humanHandoffPending || !question.trim()"
            @click="handleSend"
          >
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { askAi } from '@/api/ask'
import { ASK_AI_RESULT_TYPE } from '@/utils/constants'

const router = useRouter()
const question = ref('')
const loading = ref(false)
const humanHandoffPending = ref(false)
const messages = ref([])
const chatWindowRef = ref()

async function handleSend() {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  await send(q)
}

/**
 * 用户点击“转人工”时，直接以“转人工”作为问题发送。
 * 后端 AskAiService 会识别转人工意图并进入人工接管流程。
 */
async function escalateToHuman() {
  await send('转人工')
}

async function send(q) {
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const data = await askAi(q)
    messages.value.push({
      role: 'ai',
      resultType: data.resultType,
      answer: data.answer,
      warning: data.warning,
      canEscalate: data.canEscalate,
      references: data.references || []
    })

    if (data.resultType === ASK_AI_RESULT_TYPE.ESCALATED) {
      humanHandoffPending.value = true
      ElMessage.success('已为您转接人工客服')
    }
  } catch (error) {
    messages.value.push({
      role: 'ai',
      answer: '抱歉，服务暂时不可用，请稍后再试。',
      references: []
    })
    console.error('自助问答失败:', error)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatWindowRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.ask-ai {
  .chat-window {
    height: 480px;
    overflow-y: auto;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 6px;
    margin-bottom: 16px;

    .empty-tip {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;

      p {
        margin: 8px 0 0;
      }

      .hint {
        font-size: 13px;
      }
    }

    .chat-item {
      display: flex;
      margin-bottom: 16px;

      &.user {
        justify-content: flex-end;
      }

      &.ai {
        justify-content: flex-start;
      }

      .bubble {
        max-width: 75%;
        padding: 12px 16px;
        border-radius: 8px;
        line-height: 1.6;
        white-space: pre-wrap;
        word-break: break-word;
      }

      .user-bubble {
        background: #409eff;
        color: #fff;
      }

      .ai-bubble {
        background: #fff;
        color: #333;
        border: 1px solid #ebeef5;
      }
    }

    .references {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed #dcdfe6;

      .references-title {
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .reference-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        margin-bottom: 6px;

        .ref-title {
          flex: 1;
          color: #606266;
        }
      }
    }

    .escalate-action {
      margin-top: 12px;
      display: flex;
      align-items: center;
      gap: 4px;

      .escalate-hint {
        font-size: 13px;
        color: #909399;
      }
    }

    .escalated-info {
      margin-top: 6px;
    }
  }

  .input-area {
    .input-buttons {
      margin-top: 12px;
      text-align: right;
    }
  }

  .mt-10 {
    margin-top: 10px;
  }
}
</style>
