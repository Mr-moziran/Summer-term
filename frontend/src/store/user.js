import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register } from '@/api/auth'
import router from '@/router'
import { useNotificationStore } from './notification'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.userId)
  const username = computed(() => userInfo.value?.username)
  const role = computed(() => userInfo.value?.role)
  const homePath = computed(() => userInfo.value?.homePath)
  const isUser = computed(() => role.value === 'USER')
  const isAgent = computed(() => role.value === 'AGENT')
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function loginAction(credentials) {
    const data = await login(credentials)
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      role: data.role,
      homePath: data.homePath
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

    if (data.homePath) {
      router.push(data.homePath)
    } else if (data.role === 'USER') {
      router.push('/my-tickets')
    } else if (data.role === 'AGENT') {
      router.push('/agent/tickets')
    } else if (data.role === 'ADMIN') {
      router.push('/admin/dashboard')
    }
  }

  async function registerAction(credentials) {
    await register(credentials)
  }

  function logout() {
    // 退出前断开 WebSocket 并清空通知状态
    const notificationStore = useNotificationStore()
    notificationStore.disconnect()
    notificationStore.clearAll()

    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userId,
    username,
    role,
    homePath,
    isUser,
    isAgent,
    isAdmin,
    loginAction,
    registerAction,
    logout
  }
})
