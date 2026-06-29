import request from '@/utils/request'

/**
 * 获取工单列表
 */
export function getTickets(params) {
  return request({
    url: '/tickets',
    method: 'GET',
    params
  })
}

/**
 * 获取工单详情
 */
export function getTicketDetail(id) {
  return request({
    url: `/tickets/${id}`,
    method: 'GET'
  })
}

/**
 * 创建工单
 */
export function createTicket(data) {
  return request({
    url: '/tickets',
    method: 'POST',
    data
  })
}

/**
 * 更新工单状态
 */
export function updateTicketStatus(id, status) {
  return request({
    url: `/tickets/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 分配工单
 */
export function assignTicket(id, assigneeId) {
  return request({
    url: `/tickets/${id}/assign`,
    method: 'POST',
    data: { assigneeId }
  })
}

/**
 * 评价工单
 */
export function rateTicket(id, data) {
  return request({
    url: `/tickets/${id}/rate`,
    method: 'POST',
    data
  })
}

/**
 * 获取工单回复列表
 */
export function getTicketReplies(id) {
  return request({
    url: `/tickets/${id}/replies`,
    method: 'GET'
  })
}

/**
 * 发送工单回复
 */
export function replyTicket(id, data) {
  return request({
    url: `/tickets/${id}/replies`,
    method: 'POST',
    data
  })
}
