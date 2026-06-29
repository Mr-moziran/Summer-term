<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useNotificationStore } from '@/store/notification'

const userStore = useUserStore()
const notificationStore = useNotificationStore()

onMounted(() => {
  // 如果用户已登录，连接 WebSocket
  if (userStore.isLoggedIn) {
    notificationStore.connect()
  }
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
