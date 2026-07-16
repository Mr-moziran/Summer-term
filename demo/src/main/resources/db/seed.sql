-- 企业智能客服工单系统种子数据（SaaS 客服演示场景）
-- 数据为课程演示而整理，字段结构参考公开客服工单数据集；可重复执行。
-- 演示账号密码：admin123、agent123、user123。

INSERT INTO users (username, email, password, role, status) VALUES
    ('管理员',     'admin@example.com',  '$2b$12$1wwWqfsRjGnA7NHu3Ab7X.8MHAjcksCDoy8PJh6UUUqMA99QLM7RK', 'ADMIN', 'ACTIVE'),
    ('客服张三',   'agent@example.com',  '$2b$12$cTh6NdJ0c.a6WlZvjof38eRaDs8xe6hKlMK1ChWoCzEXo.5XkWrJi', 'AGENT', 'ACTIVE'),
    ('客服李四',   'agent2@example.com', '$2b$12$cTh6NdJ0c.a6WlZvjof38eRaDs8xe6hKlMK1ChWoCzEXo.5XkWrJi', 'AGENT', 'ACTIVE'),
    ('客服陈晨',   'agent3@example.com', '$2b$12$cTh6NdJ0c.a6WlZvjof38eRaDs8xe6hKlMK1ChWoCzEXo.5XkWrJi', 'AGENT', 'ACTIVE'),
    ('普通用户王五','user@example.com',  '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE'),
    ('测试用户赵六','user2@example.com', '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE'),
    ('企业用户钱七','user3@example.com', '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE'),
    ('试用用户孙八','user4@example.com', '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE')
ON CONFLICT (email) DO NOTHING;

DO $$
BEGIN
    -- 18 张工单：覆盖全部状态、分类、优先级与 AI 已分类/待分类两种情况。
    WITH seed(title, description, category, priority, status, submitter_email, assignee_email, ai_classified, created_at) AS (
        VALUES
            ('无法登录系统', '从昨天开始一直提示密码错误，已尝试重置密码但未收到邮件。', 'TECHNICAL', 'HIGH', 'PROCESSING', 'user@example.com', 'agent@example.com', TRUE, NOW() - INTERVAL '2 hours'),
            ('账单金额异常', '本月账单显示扣费 299 元，但套餐应为 199 元。', 'BILLING', 'URGENT', 'ASSIGNED', 'user@example.com', 'agent@example.com', TRUE, NOW() - INTERVAL '1 day'),
            ('建议增加批量导出功能', '希望能批量导出工单记录为 Excel，减少人工整理时间。', 'OTHER', 'LOW', 'PENDING', 'user2@example.com', NULL, FALSE, NOW() - INTERVAL '3 days'),
            ('客服态度投诉', '昨天与客服沟通时对方态度恶劣，要求处理。', 'COMPLAINT', 'HIGH', 'ASSIGNED', 'user2@example.com', 'agent2@example.com', FALSE, NOW() - INTERVAL '4 hours'),
            ('系统响应缓慢', '最近三天系统经常超时，影响正常工作。', 'TECHNICAL', 'MEDIUM', 'RESOLVED', 'user@example.com', 'agent@example.com', TRUE, NOW() - INTERVAL '2 days'),
            ('退款未到账', '申请退款已超过 7 个工作日仍未收到。', 'BILLING', 'URGENT', 'CLOSED', 'user2@example.com', 'agent2@example.com', TRUE, NOW() - INTERVAL '5 days'),
            ('重置密码后仍无法登录', '重置密码后页面仍提示凭证无效，换了浏览器也没有解决。', 'TECHNICAL', 'HIGH', 'PENDING', 'user3@example.com', NULL, FALSE, NOW() - INTERVAL '45 minutes'),
            ('账号被锁定', '连续输入密码后账号显示已锁定，影响团队工作。', 'TECHNICAL', 'MEDIUM', 'ASSIGNED', 'user4@example.com', 'agent@example.com', TRUE, NOW() - INTERVAL '6 hours'),
            ('邀请成员失败', '邀请新同事加入企业空间时提示成员数量已达上限。', 'OTHER', 'LOW', 'PENDING', 'user3@example.com', NULL, FALSE, NOW() - INTERVAL '7 hours'),
            ('支付订阅时提示失败', '公司信用卡支付年度订阅时被拒绝，已确认卡内余额充足。', 'BILLING', 'HIGH', 'PROCESSING', 'user4@example.com', 'agent2@example.com', TRUE, NOW() - INTERVAL '1 day 3 hours'),
            ('需要下载付款凭证', '财务报销需要下载上个月订单的付款凭证。', 'BILLING', 'LOW', 'RESOLVED', 'user3@example.com', 'agent2@example.com', TRUE, NOW() - INTERVAL '4 days'),
            ('申请取消订阅', '下个账期不再续费，想确认取消后数据保留多久。', 'BILLING', 'MEDIUM', 'ASSIGNED', 'user4@example.com', 'agent3@example.com', TRUE, NOW() - INTERVAL '3 hours'),
            ('导出报表一直失败', '导出近一年的工单报表，任务完成后没有生成下载文件。', 'TECHNICAL', 'MEDIUM', 'ASSIGNED', 'user@example.com', 'agent@example.com', TRUE, NOW() - INTERVAL '1 day 6 hours'),
            ('没有工单查看权限', '新加入团队后看不到分配给我的工单列表。', 'TECHNICAL', 'HIGH', 'PROCESSING', 'user3@example.com', 'agent3@example.com', TRUE, NOW() - INTERVAL '8 hours'),
            ('上传附件提示失败', '上传 8MB 的 PNG 截图时反复提示上传失败。', 'TECHNICAL', 'MEDIUM', 'PENDING', 'user2@example.com', NULL, FALSE, NOW() - INTERVAL '25 minutes'),
            ('数据看板加载为空', '管理员打开今日数据看板时所有统计都是空白。', 'TECHNICAL', 'URGENT', 'PROCESSING', 'user3@example.com', 'agent3@example.com', TRUE, NOW() - INTERVAL '50 minutes'),
            ('投诉问题迟迟没有回复', '三天前提交的投诉工单一直没有收到处理进展。', 'COMPLAINT', 'HIGH', 'RESOLVED', 'user4@example.com', 'agent2@example.com', TRUE, NOW() - INTERVAL '6 days'),
            ('希望支持深色模式', '夜间使用页面太亮，希望增加深色模式。', 'OTHER', 'LOW', 'CLOSED', 'user2@example.com', 'agent3@example.com', TRUE, NOW() - INTERVAL '8 days')
    )
    INSERT INTO tickets (title, description, category, priority, status, submitter_id, assignee_id, ai_classified, created_at)
    SELECT seed.title, seed.description, seed.category::VARCHAR, seed.priority::VARCHAR, seed.status::VARCHAR,
           submitter.id, assignee.id, seed.ai_classified, seed.created_at
    FROM seed
    JOIN users submitter ON submitter.email = seed.submitter_email
    LEFT JOIN users assignee ON assignee.email = seed.assignee_email
    WHERE NOT EXISTS (SELECT 1 FROM tickets ticket WHERE ticket.title = seed.title);

    UPDATE tickets
    SET resolved_at = COALESCE(resolved_at, created_at + INTERVAL '4 hours')
    WHERE title IN ('系统响应缓慢', '需要下载付款凭证', '投诉问题迟迟没有回复')
      AND status = 'RESOLVED';

    UPDATE tickets
    SET resolved_at = COALESCE(resolved_at, created_at + INTERVAL '5 hours'),
        closed_at = COALESCE(closed_at, created_at + INTERVAL '1 day')
    WHERE title IN ('退款未到账', '希望支持深色模式')
      AND status = 'CLOSED';

    -- 处理记录：包含 AI 草稿采纳与人工回复两种情况，用于客服绩效统计。
    WITH seed(ticket_title, author_email, content, ai_adopted, created_at) AS (
        VALUES
            ('无法登录系统', 'agent@example.com', '您好，已收到您的反馈。请先检查是否开启了大小写锁定，我们也在后台查看您的账号状态。', FALSE, NOW() - INTERVAL '1 hour 30 minutes'),
            ('无法登录系统', 'agent@example.com', '经排查您的账号未被锁定，建议清除浏览器缓存后重试。如仍有问题请回复此工单。', TRUE, NOW() - INTERVAL '30 minutes'),
            ('账单金额异常', 'agent@example.com', '已核实您的账单，确认是系统升级导致的计费错误，已提交修正申请。', TRUE, NOW() - INTERVAL '12 hours'),
            ('客服态度投诉', 'agent2@example.com', '非常抱歉给您带来不愉快的体验，我们已启动服务复核，并将反馈处理进展。', FALSE, NOW() - INTERVAL '2 hours'),
            ('系统响应缓慢', 'agent@example.com', '我们已定位到数据库连接池配置不足的问题，优化后响应时间已恢复正常。', TRUE, NOW() - INTERVAL '1 day 12 hours'),
            ('系统响应缓慢', 'user@example.com', '感谢处理，今天使用确实快了很多。', FALSE, NOW() - INTERVAL '1 day'),
            ('退款未到账', 'agent2@example.com', '已为您加急处理退款，预计 1 至 3 个工作日内到账。', TRUE, NOW() - INTERVAL '5 days'),
            ('退款未到账', 'agent2@example.com', '退款已于今天上午到达您的账户，请查收。如有疑问请回复。', FALSE, NOW() - INTERVAL '4 days'),
            ('退款未到账', 'user2@example.com', '已收到退款，谢谢。', FALSE, NOW() - INTERVAL '3 days'),
            ('账号被锁定', 'agent@example.com', '已收到账号锁定反馈，我们正在核验账号保护记录。请勿继续重复输入密码。', FALSE, NOW() - INTERVAL '5 hours'),
            ('支付订阅时提示失败', 'agent2@example.com', '请先确认发卡行未限制线上支付；如已扣款但订单未生效，请回复支付凭证。', TRUE, NOW() - INTERVAL '1 day'),
            ('需要下载付款凭证', 'agent2@example.com', '已在订单详情中为您标注下载路径：订阅与账单—订单记录—付款凭证。', TRUE, NOW() - INTERVAL '3 days 20 hours'),
            ('申请取消订阅', 'agent3@example.com', '已收到取消续费申请。当前账期结束后将停止续费，数据保留规则请以订阅页面说明为准。', FALSE, NOW() - INTERVAL '2 hours'),
            ('导出报表一直失败', 'agent@example.com', '已收到导出失败反馈。请先缩小导出时间范围，我们正在检查任务日志。', TRUE, NOW() - INTERVAL '1 day 5 hours'),
            ('没有工单查看权限', 'agent3@example.com', '已确认您当前角色缺少工单查看权限，正在协助空间管理员调整授权。', FALSE, NOW() - INTERVAL '7 hours'),
            ('数据看板加载为空', 'agent3@example.com', '该问题影响管理员统计使用，已按紧急故障提交技术排查。', TRUE, NOW() - INTERVAL '35 minutes'),
            ('投诉问题迟迟没有回复', 'agent2@example.com', '很抱歉未及时跟进。经复核，原工单已重新分配并补充处理记录。', FALSE, NOW() - INTERVAL '5 days 12 hours'),
            ('希望支持深色模式', 'agent3@example.com', '已记录您的深色模式建议并提交产品需求池，感谢反馈。', TRUE, NOW() - INTERVAL '7 days 12 hours')
    )
    INSERT INTO replies (ticket_id, author_id, content, is_ai_draft, ai_adopted, created_at)
    SELECT ticket.id, author.id, seed.content, FALSE, seed.ai_adopted, seed.created_at
    FROM seed
    JOIN tickets ticket ON ticket.title = seed.ticket_title
    JOIN users author ON author.email = seed.author_email
    WHERE NOT EXISTS (
        SELECT 1 FROM replies reply
        WHERE reply.ticket_id = ticket.id AND reply.content = seed.content
    );

    -- 站内通知：既有未读也有已读消息，供通知中心和未读数接口演示。
    WITH seed(user_email, notification_type, ticket_title, message, is_read, created_at) AS (
        VALUES
            ('user@example.com', 'NEW_REPLY', '无法登录系统', '您的工单「无法登录系统」有新的回复', FALSE, NOW() - INTERVAL '30 minutes'),
            ('user@example.com', 'NEW_REPLY', '账单金额异常', '您的工单「账单金额异常」有新的回复', TRUE, NOW() - INTERVAL '12 hours'),
            ('user2@example.com', 'ASSIGNED', '客服态度投诉', '您的工单「客服态度投诉」已被分配给客服李四', FALSE, NOW() - INTERVAL '4 hours'),
            ('user2@example.com', 'STATUS_CHANGE', '退款未到账', '您的工单「退款未到账」状态已变更为已关闭', TRUE, NOW() - INTERVAL '3 days'),
            ('user4@example.com', 'ASSIGNED', '申请取消订阅', '您的工单「申请取消订阅」已被分配给客服陈晨', FALSE, NOW() - INTERVAL '3 hours'),
            ('user3@example.com', 'NEW_REPLY', '没有工单查看权限', '您的工单「没有工单查看权限」有新的回复', FALSE, NOW() - INTERVAL '7 hours'),
            ('user3@example.com', 'NEW_REPLY', '数据看板加载为空', '您的工单「数据看板加载为空」有新的回复', FALSE, NOW() - INTERVAL '35 minutes'),
            ('user4@example.com', 'STATUS_CHANGE', '投诉问题迟迟没有回复', '您的工单「投诉问题迟迟没有回复」状态已更新为已解决', TRUE, NOW() - INTERVAL '5 days 12 hours')
    )
    INSERT INTO notifications (user_id, type, ticket_id, message, is_read, created_at)
    SELECT recipient.id, seed.notification_type::VARCHAR, ticket.id, seed.message, seed.is_read, seed.created_at
    FROM seed
    JOIN users recipient ON recipient.email = seed.user_email
    JOIN tickets ticket ON ticket.title = seed.ticket_title
    WHERE NOT EXISTS (
        SELECT 1 FROM notifications notification
        WHERE notification.user_id = recipient.id
          AND notification.ticket_id = ticket.id
          AND notification.message = seed.message
    );
END
$$;
