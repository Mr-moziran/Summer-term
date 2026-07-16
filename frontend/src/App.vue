<template>
  <div id="app-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header v-if="userStore.isLoggedIn" class="app-header">
        <div class="header-left">
          <span class="logo">企业智能客服工单系统</span>
        </div>
        <div class="header-right">
          <NotificationBell />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span class="dropdown-role">{{ roleLabel }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const userStore = useUserStore()

const roleLabel = computed(() => {
  const labels = { USER: '普通用户', AGENT: '客服人员', ADMIN: '管理员' }
  return labels[userStore.role] || userStore.role
})

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
    try {
      await logout()
    } catch {
      // 即使接口失败也清除本地状态
    }
    userStore.logout()
  }
}
</script>

<style lang="scss">
#app-layout {
  width: 100%;
  height: 100%;

  .el-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .app-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 60px !important;
    padding: 0 24px;
    background: #001529;
    color: #fff;

    .header-left {
      .logo {
        font-size: 18px;
        font-weight: 600;
        letter-spacing: 1px;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .user-info {
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;

        &:hover {
          color: #409eff;
        }
      }

      .dropdown-role {
        color: #909399;
        font-size: 12px;
        pointer-events: none;
        cursor: default;
      }
    }
  }

  .el-main {
    flex: 1;
    overflow-y: auto;
    padding: 0;
  }
}
</style>
