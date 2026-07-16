import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/tickets',
    redirect: '/my-tickets'
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', public: true }
  },
  // 用户路由
  {
    path: '/my-tickets',
    name: 'MyTickets',
    component: () => import('@/views/user/TicketList.vue'),
    meta: { title: '我的工单', role: 'USER' }
  },
  {
    path: '/tickets/new',
    name: 'NewTicket',
    component: () => import('@/views/user/NewTicket.vue'),
    meta: { title: '提交工单', role: 'USER' }
  },
  {
    path: '/ask-ai',
    name: 'AskAi',
    component: () => import('@/views/user/AskAi.vue'),
    meta: { title: '智能助手', role: 'USER' }
  },
  {
    path: '/tickets/:id',
    name: 'TicketDetail',
    component: () => import('@/views/user/TicketDetail.vue'),
    meta: { title: '工单详情', role: 'USER' }
  },
  // 客服路由
  {
    path: '/agent/tickets',
    name: 'AgentTickets',
    component: () => import('@/views/agent/TicketList.vue'),
    meta: { title: '工单列表', role: 'AGENT' }
  },
  {
    path: '/agent/tickets/:id',
    name: 'AgentTicketDetail',
    component: () => import('@/views/agent/TicketDetail.vue'),
    meta: { title: '处理工单', role: 'AGENT' }
  },
  // 管理员路由
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理员后台', role: 'ADMIN' }
  },
  {
    path: '/admin/agents',
    name: 'AdminAgents',
    component: () => import('@/views/admin/AgentPerformance.vue'),
    meta: { title: '客服绩效', role: 'ADMIN' }
  },
  {
    path: '/admin/knowledge',
    name: 'AdminKnowledge',
    component: () => import('@/views/admin/KnowledgeManage.vue'),
    meta: { title: '知识库管理', role: 'ADMIN' }
  },
  {
    path: '/admin/tickets',
    name: 'AdminTickets',
    component: () => import('@/views/admin/TicketManage.vue'),
    meta: { title: '工单管理', role: 'ADMIN' }
  },
  {
    path: '/admin/tickets/:id',
    name: 'AdminTicketDetail',
    component: () => import('@/views/agent/TicketDetail.vue'),
    meta: { title: '处理工单', role: 'ADMIN' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', role: 'ADMIN' }
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 企业智能客服工单系统` : '企业智能客服工单系统'

  // 公开页面直接放行
  if (to.meta.public) {
    // 如果已登录用户访问登录页，重定向到对应首页
    if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
      if (userStore.homePath) {
        next(userStore.homePath)
      } else if (userStore.isUser) {
        next('/my-tickets')
      } else if (userStore.isAgent) {
        next('/agent/tickets')
      } else if (userStore.isAdmin) {
        next('/admin/dashboard')
      }
    } else {
      next()
    }
    return
  }

  // 检查登录状态
  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 检查角色权限
  if (to.meta.role) {
    const allowedRoles = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role]
    if (!allowedRoles.includes(userStore.role)) {
      next('/login')
      return
    }
  }

  next()
})

export default router
