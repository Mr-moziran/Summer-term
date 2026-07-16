import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElNotification } from 'element-plus'
import { Client } from '@stomp/stompjs'
import { WS_URL, NOTIFICATION_DESTINATION } from '@/utils/constants'
import { useUserStore } from './user'
import {
  getNotifications,
  getUnreadCount,
  markNotificationRead
} from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const client = ref(null)

  const hasUnread = computed(() => unreadCount.value > 0)

  /**
   * 建立 STOMP over WebSocket 连接。
   * 后端使用 Spring @EnableWebSocketMessageBroker，推送目的地为 /user/queue/notifications，
   * 因此这里必须使用 STOMP 协议而非原生 WebSocket。
   */
  function connect() {
    const userStore = useUserStore()
    if (!userStore.token) return
    if (client.value && client.value.active) return

    // 连接时通过 query 参数传 token，与后端握手拦截器约定一致
    const stompClient = new Client({
      brokerURL: `${WS_URL}?token=${userStore.token}`,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        stompClient.subscribe(NOTIFICATION_DESTINATION, (frame) => {
          try {
            const data = JSON.parse(frame.body)
            handleNotification(data)
          } catch (error) {
            console.error('WebSocket 消息解析失败:', error)
          }
        })
      },
      onStompError: (frame) => {
        console.error('STOMP 错误:', frame.headers['message'])
      }
    })

    stompClient.activate()
    client.value = stompClient

    // 连接的同时拉取历史通知和未读数量
    loadNotifications()
    loadUnreadCount()
  }

  function disconnect() {
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
  }

  /**
   * 拉取历史通知列表（登录/刷新后恢复消息中心）
   */
  async function loadNotifications() {
    try {
      const data = await getNotifications({ page: 0, size: 50 })
      notifications.value = data.content || []
    } catch (error) {
      console.error('加载通知列表失败:', error)
    }
  }

  async function loadUnreadCount() {
    try {
      const data = await getUnreadCount()
      unreadCount.value = data.unreadCount || 0
    } catch (error) {
      console.error('加载未读数量失败:', error)
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

  async function markAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    // read 字段与后端 NotificationResponse 的 isRead() getter 对应
    if (notification && !notification.read) {
      try {
        await markNotificationRead(notificationId)
        notification.read = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    }
  }

  async function markAllAsRead() {
    const unread = notifications.value.filter(n => !n.read)
    for (const n of unread) {
      try {
        await markNotificationRead(n.id)
        n.read = true
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    }
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
    loadNotifications,
    loadUnreadCount,
    markAsRead,
    markAllAsRead,
    clearAll
  }
})
