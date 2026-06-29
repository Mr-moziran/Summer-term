<template>
  <div class="notification-bell" @click="togglePanel">
    <el-badge :value="notificationStore.unreadCount" :hidden="!notificationStore.hasUnread">
      <el-icon :size="24"><Bell /></el-icon>
    </el-badge>

    <el-drawer
      v-model="visible"
      title="通知中心"
      size="400px"
      direction="rtl"
    >
      <div v-if="notificationStore.notifications.length > 0" class="notification-list">
        <div
          v-for="notification in notificationStore.notifications"
          :key="notification.id"
          :class="['notification-item', { unread: !notification.isRead }]"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-header">
            <el-tag size="small" :type="getNotificationType(notification.type)">
              {{ getNotificationTypeLabel(notification.type) }}
            </el-tag>
            <span class="notification-time">{{ formatRelativeTime(notification.timestamp) }}</span>
          </div>
          <div class="notification-message">{{ notification.message }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无通知" />

      <template #footer>
        <el-button
          v-if="notificationStore.notifications.length > 0"
          type="primary"
          @click="markAllAsRead"
        >
          全部标记为已读
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/store/notification'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const notificationStore = useNotificationStore()
const visible = ref(false)

function togglePanel() {
  visible.value = !visible.value
}

function handleNotificationClick(notification) {
  notificationStore.markAsRead(notification.id)
  router.push(`/tickets/${notification.ticketId}`)
  visible.value = false
}

function markAllAsRead() {
  notificationStore.markAllAsRead()
}

function getNotificationType(type) {
  const types = {
    STATUS_CHANGE: 'primary',
    NEW_REPLY: 'success',
    ASSIGNED: 'warning'
  }
  return types[type] || 'info'
}

function getNotificationTypeLabel(type) {
  const labels = {
    STATUS_CHANGE: '状态变更',
    NEW_REPLY: '新回复',
    ASSIGNED: '工单分配'
  }
  return labels[type] || type
}
</script>

<style scoped lang="scss">
.notification-bell {
  cursor: pointer;
  padding: 8px;

  &:hover {
    background-color: #f5f7fa;
    border-radius: 4px;
  }
}

.notification-list {
  .notification-item {
    padding: 15px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    &.unread {
      background-color: #ecf5ff;
    }

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .notification-time {
        font-size: 12px;
        color: #909399;
      }
    }

    .notification-message {
      font-size: 14px;
      line-height: 1.6;
      color: #606266;
    }
  }
}
</style>
