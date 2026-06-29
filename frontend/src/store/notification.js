import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElNotification } from 'element-plus'
import { WS_URL } from '@/utils/constants'
import { useUserStore } from './user'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const ws = ref(null)
  const reconnectTimer = ref(null)

  const hasUnread = computed(() => unreadCount.value > 0)

  function connect() {
    const userStore = useUserStore()
    if (!userStore.token) return

    const wsUrl = `${WS_URL}?token=${userStore.token}`
    ws.value = new WebSocket(wsUrl)

    ws.value.onopen = () => {
      console.log('WebSocket 连接成功')
      if (reconnectTimer.value) {
        clearTimeout(reconnectTimer.value)
        reconnectTimer.value = null
      }
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleNotification(data)
      } catch (error) {
        console.error('WebSocket 消息解析失败:', error)
      }
    }

    ws.value.onerror = (error) => {
      console.error('WebSocket 错误:', error)
    }

    ws.value.onclose = () => {
      console.log('WebSocket 连接关闭')
      // 5秒后尝试重连
      reconnectTimer.value = setTimeout(() => {
        connect()
      }, 5000)
    }
  }

  function disconnect() {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
  }

  function handleNotification(data) {
    notifications.value.unshift(data)
    unreadCount.value++

    // 显示桌面通知
    ElNotification({
      title: getNotificationTitle(data.type),
      message: data.message,
      type: 'info',
      duration: 5000,
      onClick: () => {
        // 点击通知跳转到工单详情
        window.location.href = `/tickets/${data.ticketId}`
      }
    })
  }

  function getNotificationTitle(type) {
    const titles = {
      STATUS_CHANGE: '工单状态变更',
      NEW_REPLY: '新回复',
      ASSIGNED: '工单分配'
    }
    return titles[type] || '系统通知'
  }

  function markAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification && !notification.isRead) {
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  }

  function clearAll() {
    notifications.value = []
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    hasUnread,
    connect,
    disconnect,
    markAsRead,
    markAllAsRead,
    clearAll
  }
})
