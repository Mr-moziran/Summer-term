import request from '@/utils/request'

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
