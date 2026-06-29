<template>
  <div class="not-found">
    <el-result
      icon="warning"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

function goHome() {
  if (userStore.isUser) {
    router.push('/my-tickets')
  } else if (userStore.isAgent) {
    router.push('/agent/tickets')
  } else if (userStore.isAdmin) {
    router.push('/admin/dashboard')
  } else {
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
}
</style>
