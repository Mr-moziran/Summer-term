-- 基于公开 Kaggle 客服工单的“问题类型分布”改写。
-- 本文件不含原始客户个人信息、商品信息或英文原文；内容已按本项目 SaaS 业务规则重写。
-- 可重复执行。执行后重新运行 ResolvedTicketSeedIndexer，即可将已解决案例写入向量检索库。

DO $$
BEGIN
    WITH seed(title, description, category, priority, status, submitter_email, assignee_email, created_at, resolved_at, closed_at) AS (
        VALUES
            ('Chrome 浏览器中保存工单草稿失败', '在最新版 Chrome 编辑工单后点击保存，页面提示保存失败；使用无痕窗口复现，未上传附件。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '28 days', NOW() - INTERVAL '27 days 20 hours', NULL),
            ('导出 CSV 文件打开后字段错位', '导出近三个月工单 CSV 后，Excel 中的中文描述显示在错误列，影响财务整理。', 'TECHNICAL', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent@example.com', NOW() - INTERVAL '27 days', NOW() - INTERVAL '26 days 18 hours', NOW() - INTERVAL '26 days'),
            ('切换网络后页面一直显示加载中', '办公室网络切换到移动热点后，工单列表持续加载，刷新和重新登录均未恢复。', 'TECHNICAL', 'HIGH', 'RESOLVED', 'user3@example.com', 'agent3@example.com', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days 4 hours', NULL),
            ('成员权限调整后无法创建工单', '管理员将成员改为普通用户后，该成员进入系统可以查看列表但提交按钮不可用。', 'TECHNICAL', 'HIGH', 'CLOSED', 'user4@example.com', 'agent3@example.com', NOW() - INTERVAL '24 days', NOW() - INTERVAL '23 days 20 hours', NOW() - INTERVAL '23 days'),
            ('PDF 附件上传后无法预览', '上传 6MB 的 PDF 后显示上传成功，但在工单详情中点击附件没有预览或下载反应。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '22 days', NOW() - INTERVAL '21 days 18 hours', NULL),
            ('通知中心未显示新的客服回复', '客服已经回复工单，但用户登录后通知中心没有未读提醒，工单详情可以看到回复内容。', 'TECHNICAL', 'LOW', 'CLOSED', 'user2@example.com', 'agent2@example.com', NOW() - INTERVAL '21 days', NOW() - INTERVAL '20 days 18 hours', NOW() - INTERVAL '20 days'),

            ('同一订阅周期被重复扣费', '同一企业空间在同一账期内出现两笔专业版订阅扣款，均显示支付成功。', 'BILLING', 'URGENT', 'RESOLVED', 'user3@example.com', 'agent2@example.com', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days 16 hours', NULL),
            ('支付成功但套餐没有生效', '银行卡支付专业版成功后，订阅页面仍显示标准版，成员上限也没有变化。', 'BILLING', 'HIGH', 'CLOSED', 'user4@example.com', 'agent2@example.com', NOW() - INTERVAL '19 days', NOW() - INTERVAL '18 days 19 hours', NOW() - INTERVAL '18 days'),
            ('退款审核状态长时间未更新', '首次订阅未使用，在支付后三天提交退款申请；两天审核目标时间已过，订单详情仍显示审核中。', 'BILLING', 'URGENT', 'RESOLVED', 'user@example.com', 'agent2@example.com', NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days 17 hours', NULL),
            ('退款通过后原支付渠道未到账', '退款审核通过后已超过七个工作日，原支付渠道仍未收到退款。', 'BILLING', 'URGENT', 'CLOSED', 'user2@example.com', 'agent2@example.com', NOW() - INTERVAL '17 days', NOW() - INTERVAL '16 days 19 hours', NOW() - INTERVAL '16 days'),
            ('关闭自动续费后仍担心会扣款', '用户已在订阅与账单页面关闭自动续费，想确认当前已支付账期结束前是否仍可继续使用。', 'BILLING', 'LOW', 'RESOLVED', 'user3@example.com', 'agent@example.com', NOW() - INTERVAL '16 days', NOW() - INTERVAL '15 days 20 hours', NULL),
            ('升级套餐的差额计算不清楚', '标准版升级专业版后账单出现补款，用户希望了解剩余账期差额的计算方式。', 'BILLING', 'MEDIUM', 'CLOSED', 'user4@example.com', 'agent@example.com', NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days 18 hours', NOW() - INTERVAL '14 days'),
            ('申请取消订阅后仍收到续费提醒', '用户已提交取消续费请求，但邮件提醒中仍显示下个账期将自动扣款。', 'BILLING', 'HIGH', 'RESOLVED', 'user@example.com', 'agent3@example.com', NOW() - INTERVAL '14 days', NOW() - INTERVAL '13 days 21 hours', NULL),
            ('需要修改已开具发票的抬头', '付款凭证已生成，用户发现开票抬头填写错误，希望直接修改后重新下载。', 'BILLING', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent2@example.com', NOW() - INTERVAL '13 days', NOW() - INTERVAL '12 days 18 hours', NOW() - INTERVAL '12 days'),
            ('取消订阅后询问数据保留期限', '企业空间已在当前账期结束后停止续费，管理员希望确认工单数据可保留多久以及何时导出。', 'BILLING', 'MEDIUM', 'RESOLVED', 'user3@example.com', 'agent3@example.com', NOW() - INTERVAL '12 days', NOW() - INTERVAL '11 days 20 hours', NULL),
            ('首次订阅超过七天后申请退款', '用户表示订阅使用较少，希望在支付十天后申请退款，询问是否可以直接退款。', 'BILLING', 'MEDIUM', 'CLOSED', 'user4@example.com', 'agent2@example.com', NOW() - INTERVAL '11 days', NOW() - INTERVAL '10 days 18 hours', NOW() - INTERVAL '10 days'),
            ('支付页面重复提交导致订单状态异常', '网络卡顿时连续点击支付，页面显示处理中，用户担心后续会出现重复扣款。', 'BILLING', 'HIGH', 'RESOLVED', 'user@example.com', 'agent2@example.com', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days 20 hours', NULL),
            ('下载付款凭证时找不到历史订单', '用户需要上个账期的付款凭证，但在订阅与账单页面没有找到对应订单。', 'BILLING', 'LOW', 'CLOSED', 'user2@example.com', 'agent@example.com', NOW() - INTERVAL '9 days', NOW() - INTERVAL '8 days 18 hours', NOW() - INTERVAL '8 days'),

            ('咨询是否支持企业单点登录', '企业管理员询问系统是否可以通过公司统一身份认证登录，并希望了解开通条件。', 'OTHER', 'MEDIUM', 'RESOLVED', 'user3@example.com', 'agent3@example.com', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days 20 hours', NULL),
            ('希望增加工单标签筛选功能', '用户希望按项目和紧急程度添加自定义标签，以便在列表中快速筛选工单。', 'OTHER', 'LOW', 'CLOSED', 'user4@example.com', 'agent3@example.com', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days 20 hours', NOW() - INTERVAL '6 days'),
            ('咨询专业版可添加多少成员', '团队准备升级套餐，想确认专业版的成员数量上限以及超出后应如何处理。', 'OTHER', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days 20 hours', NULL),
            ('希望提供工单到期提醒', '用户希望工单接近服务目标时间时给负责人发送站内提醒，避免遗漏处理。', 'OTHER', 'LOW', 'CLOSED', 'user2@example.com', 'agent3@example.com', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days 20 hours', NOW() - INTERVAL '4 days'),
            ('咨询是否可以批量导出全部历史工单', '管理员计划导出两年工单用于归档，询问是否可以一次性导出全部历史数据。', 'OTHER', 'MEDIUM', 'RESOLVED', 'user3@example.com', 'agent@example.com', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days 20 hours', NULL),
            ('希望在夜间使用深色主题', '用户反馈夜间处理工单时页面较亮，希望提供深色主题或跟随系统设置。', 'OTHER', 'LOW', 'CLOSED', 'user4@example.com', 'agent3@example.com', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 20 hours', NOW() - INTERVAL '2 days'),
            ('咨询新成员邀请链接为何失效', '管理员上午发出的成员邀请链接在下午打开时提示失效，想确认有效期和重新发送方式。', 'OTHER', 'LOW', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day 20 hours', NULL)
    )
    INSERT INTO tickets (title, description, category, priority, status, submitter_id, assignee_id, ai_classified, created_at, resolved_at, closed_at)
    SELECT seed.title, seed.description, seed.category, seed.priority, seed.status,
           submitter.id, assignee.id, TRUE, seed.created_at, seed.resolved_at, seed.closed_at
    FROM seed
    JOIN users submitter ON submitter.email = seed.submitter_email
    JOIN users assignee ON assignee.email = seed.assignee_email
    WHERE NOT EXISTS (SELECT 1 FROM tickets ticket WHERE ticket.title = seed.title);

    WITH seed(ticket_title, author_email, content) AS (
        VALUES
            ('Chrome 浏览器中保存工单草稿失败', 'agent@example.com', '已定位为浏览器缓存导致的草稿提交异常。请清理站点缓存后重新登录；我们已完成服务端修复，后续保存可正常使用。'),
            ('导出 CSV 文件打开后字段错位', 'agent@example.com', '已修复 CSV 中包含换行描述时的转义问题。请重新发起导出；如使用 Excel，建议按 UTF-8 编码导入。'),
            ('切换网络后页面一直显示加载中', 'agent3@example.com', '已确认旧网络会话未及时刷新。请刷新页面并重新登录；当前网络切换后列表可正常加载。'),
            ('成员权限调整后无法创建工单', 'agent3@example.com', '已核实该成员角色缺少创建权限。管理员重新分配对应角色并让成员刷新页面后，已可正常提交工单。'),
            ('PDF 附件上传后无法预览', 'agent@example.com', '附件已成功保存，但预览服务短时异常。现已恢复，请重新打开工单详情下载或预览该 PDF。'),
            ('通知中心未显示新的客服回复', 'agent2@example.com', '已补发该条通知并修复未读状态同步。后续客服回复会在通知中心正常展示。'),
            ('同一订阅周期被重复扣费', 'agent2@example.com', '已核对为重复扣费并提交原支付渠道退款。退款审核通过后通常 1 至 7 个工作日到账；本工单会持续跟进。'),
            ('支付成功但套餐没有生效', 'agent2@example.com', '已核对支付记录并补齐订阅状态，专业版权益现已生效。请刷新订阅页面后确认成员上限。'),
            ('退款审核状态长时间未更新', 'agent2@example.com', '该退款已超过 2 个工作日审核目标，我们已转人工加急复核，并会在订单详情更新进度。'),
            ('退款通过后原支付渠道未到账', 'agent2@example.com', '该退款已超过通常 1 至 7 个工作日到账范围，已转人工向支付渠道核查，请保留本工单等待结果。'),
            ('关闭自动续费后仍担心会扣款', 'agent@example.com', '已确认自动续费关闭成功。当前已支付账期可继续使用，账期结束后不会自动续费。'),
            ('升级套餐的差额计算不清楚', 'agent@example.com', '升级会立即生效，系统会按当前账期剩余时间折算标准版与专业版的差额，账单中已展示本次补款明细。'),
            ('申请取消订阅后仍收到续费提醒', 'agent3@example.com', '已确认订阅不会续费；该邮件为取消前生成的提醒。当前账期结束后服务将停止续费。'),
            ('需要修改已开具发票的抬头', 'agent2@example.com', '已开具发票的修改或作废需要人工处理。我们已登记正确抬头并转交财务核验。'),
            ('取消订阅后询问数据保留期限', 'agent3@example.com', '订阅取消后数据保留 30 天，请在保留期内完成必要的数据导出；到期后将进入不可恢复删除流程。'),
            ('首次订阅超过七天后申请退款', 'agent2@example.com', '首次未使用订阅可在支付后 7 天内申请退款。本订单已超过该期限，需转人工根据实际使用情况审核。'),
            ('支付页面重复提交导致订单状态异常', 'agent2@example.com', '已核对当前仅生成一笔有效订单。遇到支付处理中请勿连续提交；如后续发现重复扣款，我们会按重复扣费流程人工核验。'),
            ('下载付款凭证时找不到历史订单', 'agent@example.com', '已协助定位历史订单。可在订阅与账单的订单记录中选择对应账期，再下载付款凭证。'),
            ('咨询是否支持企业单点登录', 'agent3@example.com', '当前演示系统未提供企业单点登录配置入口。该需求已记录为产品咨询，如需评估请由人工客服进一步确认。'),
            ('希望增加工单标签筛选功能', 'agent3@example.com', '已将标签筛选需求提交至产品需求池。是否上线及时间需以产品评估结果为准。'),
            ('咨询专业版可添加多少成员', 'agent@example.com', '专业版最多可添加 50 名成员；如成员数量超过上限，请先调整成员数量或联系人工客服评估方案。'),
            ('希望提供工单到期提醒', 'agent3@example.com', '已记录该提醒需求并提交至产品需求池。现阶段请结合工单优先级和服务目标安排跟进。'),
            ('咨询是否可以批量导出全部历史工单', 'agent@example.com', '拥有导出权限的成员可导出最近 12 个月数据。两年历史数据不支持一次性导出，建议在数据保留规则允许范围内分批处理。'),
            ('希望在夜间使用深色主题', 'agent3@example.com', '深色主题需求已记录并提交产品需求池，当前暂未提供上线时间承诺。'),
            ('咨询新成员邀请链接为何失效', 'agent@example.com', '成员邀请链接有效期为 7 天。链接过期后请由空间管理员重新发送邀请。')
    )
    INSERT INTO replies (ticket_id, author_id, content, is_ai_draft, ai_adopted, created_at)
    SELECT ticket.id, author.id, seed.content, FALSE, TRUE, ticket.resolved_at
    FROM seed
    JOIN tickets ticket ON ticket.title = seed.ticket_title
    JOIN users author ON author.email = seed.author_email
    WHERE NOT EXISTS (
        SELECT 1 FROM replies reply
        WHERE reply.ticket_id = ticket.id AND reply.content = seed.content
    );
END
$$;
