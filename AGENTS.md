# AGENTS.md — 企业智能客服工单系统

## 项目概述

Spring Boot 3.5 + Java 21 后端服务，实现企业智能客服工单系统。集成 Spring AI 对接 DeepSeek 大模型和 PgVector 向量检索，提供 AI 自动分类、AI 建议回复和用户自助问答能力。

## 目录结构

```
summer-term/
├── demo/                          # Maven 后端项目（唯一代码模块）
│   └── src/main/java/com/project/demo/
│       ├── admin/                 # 管理员统计与用户管理
│       ├── ai/                    # AI 能力层（问答、建议、向量检索、模型客户端）
│       ├── auth/                  # 注册/登录/JWT 认证
│       ├── common/                # 通用 DTO、全局异常处理
│       ├── notification/          # 站内通知（WebSocket 推送）
│       ├── reply/                 # 工单回复
│       ├── security/              # Spring Security 配置、JWT 过滤器
│       ├── ticket/                # 工单核心（CRUD、工作流、分类）
│       ├── user/                  # 用户实体与 Repository
│       └── websocket/             # WebSocket 配置与认证握手
├── docs/superpowers/              # 功能方案文档（plans/、specs/）
├── 详细方案.md                     # 系统详细开发方案
└── 数据库启动.md                   # 本地 PostgreSQL + pgvector 启动指南
```

## 构建与运行命令

```bash
# 构建（在 demo/ 目录下执行）
cd demo && ./mvnw clean package

# 运行
cd demo && ./mvnw spring-boot:run

# 运行全部测试
cd demo && ./mvnw test

# 运行单个测试类
cd demo && ./mvnw test -Dtest=AskAiServiceTests

# 运行单个测试方法
cd demo && ./mvnw test -Dtest="AskAiServiceTests#methodName"
```

## 基础设施依赖

- **数据库**: PostgreSQL + pgvector 扩展（Docker 镜像 `pgvector/pgvector:0.8.3-pg18-trixie`），默认 `localhost:5432/ticket_system`，用户名/密码 `postgres/postgres`，参考 `数据库启动.md`
- **AI 模型**: DeepSeek Chat（OpenAI 兼容接口，base-url `https://api.deepseek.com`），需设置环境变量 `DS_API_KEY`
- **向量存储**: Spring AI PgVectorStore，embedding 通过 OpenAI 兼容接口（`text-embedding-ada-002`，1536 维，余弦距离）
- **Schema 管理**: `ddl-auto: none`，手动执行 `src/main/resources/db/schema.sql` 初始化

## 架构约定

### 包组织 — 按业务能力分包
每个业务包（如 `ticket/`、`ai/`、`auth/`）内部包含 Controller、Service、Repository、DTO 等同层类，不按技术层拆分跨业务包。横切关注点放 `common/`、`security/`、`websocket/`。

### 分层规则
- **Controller**: 只做请求转发和参数校验（Bean Validation），不包含业务逻辑
- **Service**: 业务逻辑和事务边界（`@Transactional`）
- **Repository**: 继承 Spring Data JPA，纯数据访问
- **DTO**: 请求/响应对象用独立 record 类，不直接暴露实体

### API 路径约定
- `/api/auth/**` — 认证（公开）
- `/api/tickets/**` — 工单（需登录）
- `/api/ai/**` — AI 功能（AGENT/ADMIN，`/api/ai/ask` 需任意已登录用户）
- `/api/admin/**` — 管理后台（ADMIN）
- `/api/notifications/**` — 通知（需登录）
- `/ws/**` — WebSocket（公开端点，内部做 JWT 认证）

### 角色体系
`USER`（普通用户）、`AGENT`（客服人员）、`ADMIN`（管理员），定义在 `UserRole` 枚举和数据库 CHECK 约束中。

### AI 能力层接口
- `AskAiClient` — 用户自助问答模型客户端（`DeepSeekAskAiClient` / `LocalAskAiClient`）
- `TicketAiClient` — 工单分类/建议模型客户端（`DeepSeekTicketAiClient` / `LocalTicketAiClient`）
- `KnowledgeSearch` — 知识库向量检索（`PgVectorKnowledgeSearch` / `NoopKnowledgeSearch`）
- `SimilarTicketSearch` — 相似工单检索（`PgVectorSimilarTicketSearch` / `NoopSimilarTicketSearch`）
- `ResolvedTicketIndex` — 已解决工单向量索引（`PgVectorResolvedTicketIndex` / `NoopResolvedTicketIndex`）

每个接口都有 Noop 实现，通过 `application.yml` 中 `app.ai.*.provider` 切换（`local` / `deepseek` / `none`）。

## 编码规范

- **语言**: 代码注释和 commit message 使用中文
- **Java 版本**: 21，优先使用 record、sealed class、text block 等现代语法
- **每个包有 `package-info.java`**: 描述该包职责，新增包时需同步添加
- **全局异常处理**: 所有错误统一返回 `ApiErrorResponse` JSON 格式（`GlobalExceptionHandler`），新增业务异常需在其中添加对应处理器
- **测试**: 使用 `TestAuthSupport` 创建测试用户并获取 JWT Token，测试类命名 `*Tests.java`

## 敏感文档

修改以下文件前务必阅读相关文档：
- 数据库 schema 变更 → 先读 `src/main/resources/db/schema.sql` 和 `详细方案.md`
- AI 能力变更 → 先读 `docs/superpowers/plans/` 和 `docs/superpowers/specs/`
- 安全/认证变更 → 先读 `SecurityConfig.java` 中的授权规则

## 注意事项

- PgVector 的 `initialize-schema: true` 会在启动时自动创建向量表，但业务表需要手动执行 schema.sql
- AI 模型调用可能超时或限流，相关 Service 需考虑容错和降级
- WebSocket 认证依赖自定义 `WebSocketAuthenticationInterceptor`，不走 Spring Security 标准过滤器链
