import request from '@/utils/request'

/**
 * 获取AI回复建议
 */
export function getAiSuggestion(ticketId) {
  return request({
    url: `/ai/suggest/${ticketId}`,
    method: 'GET'
  })
}
