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

-- 第二批：继续依据公开数据集的问题类型分布改写，不保留原始英文文本或个人信息。
DO $$
BEGIN
    WITH seed(title, description, category, priority, status, submitter_email, assignee_email, created_at, resolved_at, closed_at) AS (
        VALUES
            ('Edge 下载工单附件后文件无法打开', '用户在最新版 Edge 下载工单中的 PDF 附件后，系统提示文件已下载，但本地打开时显示文件损坏。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '42 days', NOW() - INTERVAL '41 days 20 hours', NULL),
            ('Firefox 导出完成后没有下载入口', '用户在最新版 Firefox 发起近六个月工单导出，任务状态显示完成，但页面没有出现下载按钮。', 'TECHNICAL', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent2@example.com', NOW() - INTERVAL '41 days', NOW() - INTERVAL '40 days 18 hours', NOW() - INTERVAL '40 days'),
            ('附件文件名含特殊字符导致上传失败', '用户上传 2MB 的 PNG 截图时提示上传失败，文件名中包含括号和多个特殊符号。', 'TECHNICAL', 'LOW', 'RESOLVED', 'user3@example.com', 'agent3@example.com', NOW() - INTERVAL '40 days', NOW() - INTERVAL '39 days 20 hours', NULL),
            ('密码重置链接打开后提示失效', '用户收到密码重置邮件后半小时内打开链接，页面提示链接失效，且此前曾重复申请过重置。', 'TECHNICAL', 'HIGH', 'CLOSED', 'user4@example.com', 'agent@example.com', NOW() - INTERVAL '39 days', NOW() - INTERVAL '38 days 19 hours', NOW() - INTERVAL '38 days'),
            ('连续输错密码后账号被锁定', '成员连续多次输入错误密码后无法登录，希望立即解除锁定并继续处理工单。', 'TECHNICAL', 'HIGH', 'RESOLVED', 'user@example.com', 'agent2@example.com', NOW() - INTERVAL '38 days', NOW() - INTERVAL '37 days 20 hours', NULL),
            ('角色变更后工单权限没有更新', '空间管理员将成员调整为客服人员后，该成员仍只能查看自己提交的工单，无法处理已分配工单。', 'TECHNICAL', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent3@example.com', NOW() - INTERVAL '37 days', NOW() - INTERVAL '36 days 18 hours', NOW() - INTERVAL '36 days'),
            ('正式窗口编辑工单异常但无痕窗口正常', '用户在 Chrome 正式窗口编辑工单时保存失败，而无痕窗口可以正常保存，未上传附件。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user3@example.com', 'agent@example.com', NOW() - INTERVAL '36 days', NOW() - INTERVAL '35 days 20 hours', NULL),
            ('导出任务长时间停留在生成中', '管理员导出最近十二个月工单数据，等待十分钟后任务仍显示生成中，页面没有错误提示。', 'TECHNICAL', 'HIGH', 'CLOSED', 'user4@example.com', 'agent2@example.com', NOW() - INTERVAL '35 days', NOW() - INTERVAL '34 days 18 hours', NOW() - INTERVAL '34 days'),
            ('9MB JPG 附件上传进度一直不结束', '用户上传 9MB 的 JPG 截图，上传进度停在末尾且刷新后工单中没有附件记录。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent3@example.com', NOW() - INTERVAL '34 days', NOW() - INTERVAL '33 days 20 hours', NULL),
            ('客服回复后工单详情未自动刷新', '客服已回复工单，用户停留在详情页时没有看到新内容，手动刷新页面后才出现回复。', 'TECHNICAL', 'LOW', 'CLOSED', 'user2@example.com', 'agent@example.com', NOW() - INTERVAL '33 days', NOW() - INTERVAL '32 days 18 hours', NOW() - INTERVAL '32 days'),

            ('降级套餐后本月价格没有变化', '用户将专业版调整为标准版后，订阅页面仍显示专业版，本月账单金额也未立即下降。', 'BILLING', 'LOW', 'RESOLVED', 'user3@example.com', 'agent2@example.com', NOW() - INTERVAL '32 days', NOW() - INTERVAL '31 days 20 hours', NULL),
            ('首次订阅五天未使用申请退款', '用户在支付后第五天申请退款，表示尚未添加成员也未使用服务，希望确认退款流程。', 'BILLING', 'MEDIUM', 'CLOSED', 'user4@example.com', 'agent3@example.com', NOW() - INTERVAL '31 days', NOW() - INTERVAL '30 days 18 hours', NOW() - INTERVAL '30 days'),
            ('支付失败提示后银行账单出现扣款', '订阅结算页提示支付失败，但用户的支付渠道显示一笔待确认扣款，套餐尚未生效。', 'BILLING', 'URGENT', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '30 days', NOW() - INTERVAL '29 days 20 hours', NULL),
            ('退款通过三天后仍未到账', '订单详情显示退款审核通过，距离通过已过去三个工作日，原支付渠道暂未显示退款记录。', 'BILLING', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent2@example.com', NOW() - INTERVAL '29 days', NOW() - INTERVAL '28 days 18 hours', NOW() - INTERVAL '28 days'),
            ('续费订单希望直接申请退款', '用户的自动续费订单已扣款，希望因为暂时不用服务而立即原路退款。', 'BILLING', 'MEDIUM', 'RESOLVED', 'user3@example.com', 'agent3@example.com', NOW() - INTERVAL '28 days', NOW() - INTERVAL '27 days 20 hours', NULL),
            ('开票前需要更正接收邮箱', '用户已支付订阅但尚未申请开票，发现开票接收邮箱填写错误，希望先修改再开具。', 'BILLING', 'LOW', 'CLOSED', 'user4@example.com', 'agent@example.com', NOW() - INTERVAL '27 days', NOW() - INTERVAL '26 days 18 hours', NOW() - INTERVAL '26 days'),
            ('取消订阅后需要立即导出数据', '管理员已取消续费，担心团队工单数据被清除，希望确认仍可导出的时间范围。', 'BILLING', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent2@example.com', NOW() - INTERVAL '26 days', NOW() - INTERVAL '25 days 20 hours', NULL),
            ('标准版成员达到上限后无法邀请', '标准版空间已有十名成员，管理员继续发送邀请时提示无法添加，询问是否需要升级套餐。', 'BILLING', 'MEDIUM', 'CLOSED', 'user2@example.com', 'agent3@example.com', NOW() - INTERVAL '25 days', NOW() - INTERVAL '24 days 18 hours', NOW() - INTERVAL '24 days'),

            ('管理员移除成员后历史工单归属不清', '空间管理员移除离职成员后，希望确认该成员创建的历史工单是否仍可查看和继续分配。', 'OTHER', 'LOW', 'RESOLVED', 'user3@example.com', 'agent@example.com', NOW() - INTERVAL '24 days', NOW() - INTERVAL '23 days 20 hours', NULL),
            ('普通成员没有导出按钮', '普通成员需要导出自己负责的工单，但页面没有导出入口，询问是否为系统故障。', 'OTHER', 'MEDIUM', 'CLOSED', 'user4@example.com', 'agent2@example.com', NOW() - INTERVAL '23 days', NOW() - INTERVAL '22 days 18 hours', NOW() - INTERVAL '22 days'),
            ('导出十二个月数据需要多久', '管理员准备导出最近十二个月的全部工单，想确认是否支持该范围以及任务大致处理方式。', 'OTHER', 'LOW', 'RESOLVED', 'user@example.com', 'agent3@example.com', NOW() - INTERVAL '22 days', NOW() - INTERVAL '21 days 20 hours', NULL),
            ('希望增加工单模板功能', '用户希望为常见问题保存工单标题和描述模板，以减少重复填写时间。', 'OTHER', 'LOW', 'CLOSED', 'user2@example.com', 'agent@example.com', NOW() - INTERVAL '21 days', NOW() - INTERVAL '20 days 18 hours', NOW() - INTERVAL '20 days'),
            ('投诉客服回复未解决问题', '用户认为客服回复没有回应工单中的关键问题，希望由其他人员重新核查处理过程。', 'OTHER', 'HIGH', 'RESOLVED', 'user3@example.com', 'agent2@example.com', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days 20 hours', NULL),
            ('数据保留期结束后请求恢复工单', '企业取消订阅超过三十天后希望恢复此前工单记录，管理员表示没有提前导出。', 'OTHER', 'HIGH', 'CLOSED', 'user4@example.com', 'agent3@example.com', NOW() - INTERVAL '19 days', NOW() - INTERVAL '18 days 18 hours', NOW() - INTERVAL '18 days'),
            ('邀请链接过期后需要重新发送', '管理员发现成员邀请链接超过有效期后无法打开，希望为该成员重新发送新的邀请。', 'OTHER', 'LOW', 'RESOLVED', 'user@example.com', 'agent@example.com', NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days 20 hours', NULL)
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
            ('Edge 下载工单附件后文件无法打开', 'agent@example.com', '已重新生成该 PDF 附件并确认下载正常。后续如再次出现，请使用最新版浏览器重新下载，并避免在下载未完成时打开文件。'),
            ('Firefox 导出完成后没有下载入口', 'agent2@example.com', '已确认导出文件已生成。清理浏览器缓存并重新登录后，下载入口已正常显示；如导出范围较大，请耐心等待任务完成。'),
            ('附件文件名含特殊字符导致上传失败', 'agent3@example.com', '已将附件文件名改为仅含中文、字母和数字后上传成功。附件支持 PNG、JPG、PDF，单文件请保持在 10MB 以内。'),
            ('密码重置链接打开后提示失效', 'agent@example.com', '重复申请会使旧重置链接立即失效。已重新发送密码重置邮件，请在 30 分钟内使用最新一封邮件中的链接完成操作。'),
            ('连续输错密码后账号被锁定', 'agent2@example.com', '该账号因连续 5 次输错密码被锁定。已引导用户通过密码重置解除锁定；请勿继续重复尝试旧密码。'),
            ('角色变更后工单权限没有更新', 'agent3@example.com', '角色已调整成功。成员重新登录后权限已刷新，现可处理分配给自己的工单。'),
            ('正式窗口编辑工单异常但无痕窗口正常', 'agent@example.com', '问题由浏览器缓存中的旧页面资源引起。清理站点缓存并重新登录后，正式窗口已可正常保存工单。'),
            ('导出任务长时间停留在生成中', 'agent2@example.com', '该导出任务范围较大，已由客服核查并重新发起。导出最近 12 个月的数据可能需要数分钟，完成后请在导出入口下载。'),
            ('9MB JPG 附件上传进度一直不结束', 'agent3@example.com', '已确认附件未写入成功。重新压缩图片并刷新页面后上传完成；单个 PNG、JPG、PDF 附件不得超过 10MB。'),
            ('客服回复后工单详情未自动刷新', 'agent@example.com', '回复通知已正常发送，详情页未刷新由旧页面会话导致。刷新页面后可看到新回复，后续通知同步已恢复。'),
            ('降级套餐后本月价格没有变化', 'agent2@example.com', '降级会在当前已付费账期结束后生效，因此本月仍保留专业版权益和原套餐价格。下个账期起将按标准版结算。'),
            ('首次订阅五天未使用申请退款', 'agent3@example.com', '该订单符合首次订阅、支付后 7 天内且未使用的条件，已协助在订单详情提交退款申请，审核目标时限为 2 个工作日。'),
            ('支付失败提示后银行账单出现扣款', 'agent@example.com', '为避免重复扣款，请勿再次连续提交支付。已转人工核验订单号、支付时间和渠道；待核实后会同步套餐状态或退款处理结果。'),
            ('退款通过三天后仍未到账', 'agent2@example.com', '退款审核通过后原支付渠道通常需要 1 至 7 个工作日到账。当前仍在通常到账范围内，请继续关注支付渠道记录；超过范围可转人工核查。'),
            ('续费订单希望直接申请退款', 'agent3@example.com', '续费订单是否退款需要人工结合实际使用情况审核，无法直接承诺退款。已登记订单信息并转交人工客服核验。'),
            ('开票前需要更正接收邮箱', 'agent@example.com', '发票尚未开具，已协助更新接收邮箱。请在提交开票申请前再次确认抬头、税号和接收邮箱信息。'),
            ('取消订阅后需要立即导出数据', 'agent2@example.com', '取消订阅后数据仍保留 30 天。已确认管理员可在保留期内导出所需工单数据，请尽快完成归档。'),
            ('标准版成员达到上限后无法邀请', 'agent3@example.com', '标准版最多支持 10 名成员。当前达到上限后无法继续邀请；如需增加成员，请评估升级到最多 50 名成员的专业版。'),
            ('管理员移除成员后历史工单归属不清', 'agent@example.com', '已确认历史工单仍保留在空间内，可由管理员查看并重新分配给现有客服继续处理。移除成员不会删除历史工单记录。'),
            ('普通成员没有导出按钮', 'agent2@example.com', '导出功能需要空间管理员授予对应权限。该成员当前未具备导出权限，请由管理员调整权限后重新登录。'),
            ('导出十二个月数据需要多久', 'agent3@example.com', '拥有导出权限的成员可导出最近 12 个月数据。范围较大时任务可能需要数分钟，完成后可在页面下载导出文件。'),
            ('希望增加工单模板功能', 'agent@example.com', '已记录工单模板的使用场景、期望效果和当前替代方式，并提交至产品需求池；是否上线及时间需以产品评估结果为准。'),
            ('投诉客服回复未解决问题', 'agent2@example.com', '已将投诉转交非当事客服复核。请在原工单补充具体时间、回复内容和未解决的问题，复核结果会在工单中更新。'),
            ('数据保留期结束后请求恢复工单', 'agent3@example.com', '订阅取消后数据仅保留 30 天，期满后进入不可恢复删除流程。该空间已超过保留期，无法恢复历史工单；后续请在保留期内完成导出。'),
            ('邀请链接过期后需要重新发送', 'agent@example.com', '成员邀请链接有效期为 7 天。旧链接过期后已失效，管理员重新发送新邀请后即可使用。')
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
