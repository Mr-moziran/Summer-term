import request from '@/utils/request'

/**
 * 获取通知列表（分页）
 * @param {object} params - { unreadOnly, page, size, sort }
 */
export function getNotifications(params) {
  return request({
    url: '/notifications',
    method: 'GET',
    params
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/notifications/unread-count',
    method: 'GET'
  })
}

/**
 * 标记单条通知为已读
 */
export function markNotificationRead(id) {
  return request({
    url: `/notifications/${id}/read`,
    method: 'PATCH'
  })
}
