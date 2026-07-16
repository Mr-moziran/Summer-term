# 用户自助问答设计

## 背景

当前 AI 能力主要服务客服处理页：客服或管理员通过 `/api/ai/suggest/{ticketId}` 获取工单分类、相似历史工单和回复草稿。新能力面向普通登录用户，目标是在创建人工工单前先尝试基于知识库自助解答；当用户明确要求转人工，或知识库资料不足以支撑回答时，系统自动创建转人工工单。

## 目标

- 用户必须登录后才能使用自助问答。
- AI 回答必须严格基于知识库检索结果，不能脱离资料自由发挥。
- 知识库命中高置信时直接回答。
- 知识库命中中置信时回答，并提示“该回答基于现有知识库资料生成，可能不完整”。
- 知识库低置信或无命中时，不生成不可靠答案，自动创建人工工单。
- 用户输入“转人工”“找客服”“人工处理”“没解决”“我要投诉”等相似意图时，由 LLM 通过受控工具调用触发建单。
- 明显不属于企业 SaaS 客服场景的问题（如天气、新闻、股票、娱乐）必须直接拒答，不检索知识库、不调用模型且不创建工单。
- 创建工单时提交人固定为当前登录用户，LLM 不能指定提交人或处理人。
- 首版不新增表结构，转人工上下文写入现有 `ticket.description`。

## 非目标

- 不实现未登录用户问答。
- 不新增会话历史表。
- 不新增知识库管理后台。
- 不改变现有客服处理页 `/api/ai/suggest/{ticketId}`。
- 不修改 `schema.sql` 中的工单主表结构。

## API 设计

新增接口：

```http
POST /api/ai/ask
Authorization: Bearer <token>
Content-Type: application/json
```

安全规则：

- `POST /api/ai/ask` 允许所有已登录用户访问。
- 现有 `/api/ai/suggest/{ticketId}` 仍只允许 `AGENT` 和 `ADMIN` 访问。
- `SecurityConfig` 中需要把 `/api/ai/ask` 的 `authenticated()` 规则放在 `/api/ai/**` 角色规则之前，避免普通用户被现有 AI 规则拦截。

请求体：

```json
{
  "question": "我忘记密码怎么办？"
}
```

响应体统一使用一个结果对象，通过 `resultType` 区分结果：

```json
{
  "resultType": "ANSWERED",
  "answer": "请在登录页点击忘记密码，并按照邮箱验证码完成重置。",
  "warning": null,
  "canEscalate": true,
  "references": [
    {
      "title": "账号登录帮助",
      "score": 0.89
    }
  ],
  "ticket": null
}
```

中置信回答：

```json
{
  "resultType": "ANSWERED_WITH_WARNING",
  "answer": "根据现有资料，您可以先尝试重置密码并清理浏览器缓存。",
  "warning": "该回答基于现有知识库资料生成，可能不完整。如未解决，可输入“转人工”继续处理。",
  "canEscalate": true,
  "references": [
    {
      "title": "登录问题处理说明",
      "score": 0.76
    }
  ],
  "ticket": null
}
```

超出业务范围：

```json
{
  "resultType": "OUT_OF_SCOPE",
  "answer": "抱歉，我只能协助本系统的账号登录、套餐账单、退款、工单、权限和功能使用问题。",
  "warning": null,
  "canEscalate": false,
  "references": [],
  "ticket": null
}
```

自动转人工：

```json
{
  "resultType": "ESCALATED",
  "answer": null,
  "warning": null,
  "canEscalate": false,
  "references": [],
  "ticket": {
    "id": 123,
    "status": "PENDING",
    "title": "AI未能解答：我忘记密码怎么办？"
  }
}
```

## 后端模块

新增或扩展 `com.project.demo.ai`：

- `AskAiController`：接收 `/api/ai/ask` 请求，要求登录用户。
- `AskAiService`：编排知识库检索、LLM 回答、工具调用和工单创建。
- `AskAiClient`：面向自助问答的模型端口，负责基于检索资料回答或请求工具调用。
- `KnowledgeSearch`：知识库检索端口，返回知识片段、标题、分数和来源信息。
- `AskAiRequest`、`AskAiResponse`、`KnowledgeReferenceResponse`、`EscalatedTicketResponse`：接口 DTO。
- `CreateHumanTicketTool` 或等价内部工具对象：LLM 只能通过该工具表达建单意图，实际建单由后端执行。

复用现有模块：

- `CurrentUserService`：获取当前登录用户。
- `TicketService.createTicket(...)`：创建 `PENDING` 工单。
- `TicketRepository` / `TicketResponse`：返回工单基本信息。
- `GlobalExceptionHandler`：沿用统一错误响应。

## 决策流程

1. `AskAiController` 调用 `CurrentUserService.getCurrentUser()`。
2. `AskAiService` 校验问题非空并裁剪长度。
3. 后端先判断问题是否明显超出企业 SaaS 客服支持范围；若超出，直接返回 `OUT_OF_SCOPE`，不检索、不调用模型、不建单。
4. `KnowledgeSearch` 根据问题检索知识库。
5. 后端根据最高相似度做初步置信度分级：
   - 高置信：允许 LLM 基于资料回答。
   - 中置信：允许 LLM 基于资料回答，但响应必须带 warning。
   - 低置信或无资料：跳过自由回答，直接创建人工工单。
6. 如果用户文本包含明显转人工意图，LLM 应调用建单工具；后端也保留关键词兜底判断。
7. LLM 返回回答或工具调用结果。
8. 如果发生工具调用，后端使用当前用户创建工单。
9. 返回 `ANSWERED`、`ANSWERED_WITH_WARNING`、`OUT_OF_SCOPE` 或 `ESCALATED`。

置信度阈值首版写入配置，建议默认：

- `app.ai.ask.high-threshold=0.82`
- `app.ai.ask.medium-threshold=0.70`

低于 `medium-threshold` 视为无法可靠回答。

## 工具调用约束

LLM 可请求的工具参数只允许包含：

```json
{
  "suggestedTitle": "登录问题需要人工协助",
  "reason": "用户明确要求转人工或知识库资料不足",
  "questionSummary": "忘记密码无法登录"
}
```

后端必须忽略任何提交人、处理人、状态、优先级等由模型生成的越权字段。实际建单规则：

- `submitter = 当前登录用户`
- `assignee = null`
- `status = PENDING`
- `title = AI未能解答：{问题摘要}`，超长时截断
- `description` 包含原始问题、转人工原因、知识库命中摘要、AI 处理结果

## Prompt 约束

系统 Prompt 必须包含：

- 只能使用提供的知识库片段回答。
- 不得引入片段外的事实、政策、步骤或承诺。
- 用户明确要求人工处理时，调用建单工具。
- 知识库资料不足时，调用建单工具。
- 中置信资料可回答，但必须提示可能不完整。
- 不要把工具调用结果伪装成普通回答。

第三方模型响应作为不可信输入处理。后端需要校验结构、空值、长度和工具参数，不直接信任模型输出。

## 错误处理

- 未登录：由安全过滤链返回 401。
- 无效请求体或空问题：返回 400，复用 `ApiErrorResponse`。
- LLM 调用失败：
  - 如果有中高置信知识库资料，可以返回固定兜底提示并允许用户输入“转人工”。
  - 如果没有可靠资料，创建人工工单并返回 `ESCALATED`。
- 工单创建失败：抛出业务异常，由全局异常处理器返回统一错误。

## 测试计划

- 控制器测试：未登录访问 `/api/ai/ask` 返回 401。
- 服务测试：高置信资料返回 `ANSWERED`。
- 服务测试：中置信资料返回 `ANSWERED_WITH_WARNING` 且包含 warning。
- 服务测试：低置信资料自动创建工单并返回 `ESCALATED`。
- 服务测试：用户输入“转人工”时创建工单，即使知识库有命中。
- 服务测试：模型工具参数不能覆盖当前登录用户。
- 集成测试：创建的转人工工单可在现有工单列表中查询到。

## 实施顺序

1. 增加 DTO、结果枚举和知识库检索上下文模型。
2. 增加 `KnowledgeSearch` 端口和基于现有向量检索的实现或空实现。
3. 增加 `AskAiClient` 端口和本地规则实现，先让测试闭环。
4. 增加 DeepSeek/OpenAI 兼容实现，支持工具调用。
5. 增加 `AskAiService` 编排逻辑和工单创建逻辑。
6. 增加 `/api/ai/ask` 控制器和安全配置。
7. 补齐单元测试和集成测试。
