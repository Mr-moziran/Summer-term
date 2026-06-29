# 企业智能客服工单系统 - 前端

这是企业智能客服工单系统的前端部分，使用 Vue 3 + Element Plus 开发。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **图表库**: ECharts 5
- **样式**: SCSS

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── auth.js       # 认证接口
│   │   ├── ticket.js     # 工单接口
│   │   ├── ai.js         # AI 接口
│   │   └── admin.js      # 管理员接口
│   ├── assets/           # 静态资源
│   │   └── styles/       # 样式文件
│   ├── components/       # 通用组件
│   │   ├── NotificationBell.vue    # 通知铃铛
│   │   └── TicketStatusBadge.vue   # 工单状态标签
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义和守卫
│   ├── store/            # 状态管理
│   │   ├── user.js       # 用户状态
│   │   └── notification.js  # 通知状态
│   ├── utils/            # 工具函数
│   │   ├── request.js    # Axios 封装
│   │   ├── constants.js  # 常量定义
│   │   └── format.js     # 格式化函数
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   ├── NotFound.vue  # 404页面
│   │   ├── user/         # 用户端页面
│   │   │   ├── TicketList.vue    # 工单列表
│   │   │   ├── NewTicket.vue     # 提交工单
│   │   │   └── TicketDetail.vue  # 工单详情
│   │   ├── agent/        # 客服端页面
│   │   │   ├── TicketList.vue    # 工单列表
│   │   │   └── TicketDetail.vue  # 工单处理
│   │   └── admin/        # 管理员页面
│   │       ├── Dashboard.vue     # 数据大盘
│   │       ├── TicketManage.vue  # 工单管理
│   │       └── UserManage.vue    # 用户管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── vite.config.js        # Vite 配置
└── package.json          # 项目配置
```

## 功能模块

### 1. 公共功能
- ✅ 用户登录/注册
- ✅ JWT Token 鉴权
- ✅ 路由守卫（角色权限控制）
- ✅ Axios 拦截器（请求/响应处理）
- ✅ WebSocket 实时通知

### 2. 用户端（USER）
- ✅ 我的工单列表（筛选、分页）
- ✅ 提交工单
- ✅ 查看工单详情
- ✅ 查看处理记录
- ✅ 工单评价

### 3. 客服端（AGENT）
- ✅ 待处理工单列表
- ✅ 工单详情查看
- ✅ AI 回复建议
- ✅ 相似历史工单展示
- ✅ 发送回复
- ✅ 更新工单状态

### 4. 管理员端（ADMIN）
- ✅ 数据大盘（统计图表）
- ✅ 工单管理（分配工单）
- ✅ 用户管理（启用/禁用）

## 开发指南

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器将在 http://localhost:5173 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## API 配置

后端 API 地址在 `vite.config.js` 中配置代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true
    }
  }
}
```

## 开发规范

### 1. 代码风格
- 使用 Composition API
- 使用 `<script setup>` 语法
- 使用 SCSS 编写样式
- 组件使用 PascalCase 命名

### 2. 目录规范
- 页面组件放在 `views/` 目录
- 通用组件放在 `components/` 目录
- API 接口按模块拆分到 `api/` 目录

### 3. 状态管理
- 使用 Pinia 管理全局状态
- 用户信息存储在 `user` store
- 通知信息存储在 `notification` store

### 4. 路由守卫
- 公开页面设置 `meta: { public: true }`
- 需要权限的页面设置 `meta: { role: 'USER|AGENT|ADMIN' }`

## 与后端联调

### Mock 数据阶段（第1-6周）
使用 Apifox Mock Server：
1. 在 `vite.config.js` 中将 API 代理地址指向 Mock Server
2. 前端独立开发，不依赖后端进度

### 真实接口联调（第7周）
1. 后端开发完成后，将 API 代理地址改为真实后端地址
2. 逐个接口测试验证
3. 修复字段不匹配等问题

## 常见问题

### 1. 跨域问题
开发环境已配置 Vite 代理，无跨域问题。生产环境需要后端配置 CORS。

### 2. Token 失效
Token 失效时会自动跳转到登录页，无需手动处理。

### 3. WebSocket 连接失败
检查后端 WebSocket 服务是否启动，确认 URL 配置正确。

## 项目进度

- [x] 第1周：项目初始化、基础配置
- [x] 第2周：登录/注册页、用户工单列表
- [x] 第3周：提交工单页、客服工单列表
- [x] 第4周：客服工单处理页、AI 建议面板
- [x] 第5周：管理员工单管理、用户管理
- [x] 第6周：数据大盘、WebSocket 通知
- [ ] 第7周：与后端联调
- [ ] 第8周：UI 细节优化、项目报告

## 贡献者

- 人员 B（前端开发）

## 许可证

MIT
