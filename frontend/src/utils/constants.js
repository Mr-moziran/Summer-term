// 工单状态
export const TICKET_STATUS = {
  PENDING: { label: '待处理', type: 'info' },
  ASSIGNED: { label: '已分配', type: 'primary' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' }
}

// 工单类型
export const TICKET_CATEGORY = {
  TECHNICAL: { label: '技术问题', color: '#409EFF' },
  BILLING: { label: '账单问题', color: '#67C23A' },
  COMPLAINT: { label: '投诉建议', color: '#E6A23C' },
  OTHER: { label: '其他', color: '#909399' }
}

// 优先级
export const TICKET_PRIORITY = {
  LOW: { label: '低', type: 'info' },
  MEDIUM: { label: '中', type: 'primary' },
  HIGH: { label: '高', type: 'warning' },
  URGENT: { label: '紧急', type: 'danger' }
}

// 用户角色
export const USER_ROLE = {
  USER: '普通用户',
  AGENT: '客服人员',
  ADMIN: '管理员'
}

// 通知类型
export const NOTIFICATION_TYPE = {
  STATUS_CHANGE: '状态变更',
  NEW_REPLY: '新回复',
  ASSIGNED: '工单分配'
}

// 自助问答结果类型（对应后端 AskAiResultType）
export const ASK_AI_RESULT_TYPE = {
  ANSWERED: 'ANSWERED',                       // 高置信，直接回答
  ANSWERED_WITH_WARNING: 'ANSWERED_WITH_WARNING', // 中置信，回答但带提示
  ESCALATED: 'ESCALATED'                      // 已转人工，等待客服接管
}

// WebSocket URL（STOMP 端点，对应后端 registerStompEndpoints("/ws/notifications")）
export const WS_URL = import.meta.env.MODE === 'production'
  ? 'wss://your-domain.com/ws/notifications'
  : 'ws://localhost:8080/ws/notifications'

// STOMP 订阅目的地（对应后端 convertAndSendToUser + "/queue/notifications" + userDestinationPrefix "/user"）
export const NOTIFICATION_DESTINATION = '/user/queue/notifications'
