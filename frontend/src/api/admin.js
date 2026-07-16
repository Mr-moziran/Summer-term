import request from '@/utils/request'

/**
 * 获取管理员后台面板
 */
export function getDashboard() {
  return request({
    url: '/admin/dashboard',
    method: 'GET'
  })
}

/**
 * 获取统计数据
 */
export function getStats() {
  return request({
    url: '/admin/stats',
    method: 'GET'
  })
}

/**
 * 获取客服绩效统计
 */
export function getAgentPerformance() {
  return request({
    url: '/admin/stats/agents',
    method: 'GET'
  })
}

/**
 * 上传知识库文档
 */
export function uploadKnowledgeDocument(file, title) {
  const formData = new FormData()
  formData.append('file', file)
  if (title) {
    formData.append('title', title)
  }

  return request({
    url: '/admin/knowledge-documents',
    method: 'POST',
    data: formData
  })
}

/**
 * 获取用户列表
 */
export function getUsers(params) {
  return request({
    url: '/admin/users',
    method: 'GET',
    params
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}
