import request from '@/utils/request'

/**
 * 用户自助问答
 * @param {string} question - 用户问题
 * @returns {Promise} - { resultType, answer, warning, canEscalate, references, ticket }
 */
export function askAi(question) {
  return request({
    url: '/ai/ask',
    method: 'POST',
    data: { question }
  })
}
