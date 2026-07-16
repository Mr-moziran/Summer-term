-- 企业智能客服工单系统种子数据
-- 使用匿名函数动态查找用户/工单 ID，可重复执行不会因 ID 变化而报错
-- 密码均为明文对应的 BCrypt(12) 哈希
--
-- admin123, agent123, user123

-- ===== 用户 =====
INSERT INTO users (username, email, password, role, status) VALUES
    ('管理员',     'admin@example.com',  '$2b$12$1wwWqfsRjGnA7NHu3Ab7X.8MHAjcksCDoy8PJh6UUUqMA99QLM7RK', 'ADMIN', 'ACTIVE'),
    ('客服张三',   'agent@example.com',  '$2b$12$cTh6NdJ0c.a6WlZvjof38eRaDs8xe6hKlMK1ChWoCzEXo.5XkWrJi', 'AGENT', 'ACTIVE'),
    ('客服李四',   'agent2@example.com', '$2b$12$cTh6NdJ0c.a6WlZvjof38eRaDs8xe6hKlMK1ChWoCzEXo.5XkWrJi', 'AGENT', 'ACTIVE'),
    ('普通用户王五','user@example.com',  '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE'),
    ('测试用户赵六','user2@example.com', '$2b$12$Ska2jKKsrlnkGUHStf8wleV6yDIbrlu62t6LSmio.zfJ55vF1Smy2', 'USER',  'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- ===== 工单、回复、通知（动态 ID，可重复执行） =====
DO
'
DECLARE
    u1 BIGINT; u2 BIGINT; a1 BIGINT; a2 BIGINT;
    t1 BIGINT; t2 BIGINT; t3 BIGINT; t4 BIGINT; t5 BIGINT; t6 BIGINT;
BEGIN
    -- 查用户 ID
    SELECT id INTO u1 FROM users WHERE email = ''user@example.com'';
    SELECT id INTO u2 FROM users WHERE email = ''user2@example.com'';
    SELECT id INTO a1 FROM users WHERE email = ''agent@example.com'';
    SELECT id INTO a2 FROM users WHERE email = ''agent2@example.com'';

    IF u1 IS NULL OR u2 IS NULL OR a1 IS NULL OR a2 IS NULL THEN
        RAISE EXCEPTION ''种子用户未找到，请先确认用户数据已插入'';
    END IF;

    -- 工单（已存在则跳过）
    IF NOT EXISTS (SELECT 1 FROM tickets WHERE title = ''无法登录系统'') THEN
        INSERT INTO tickets (title, description, category, priority, status, submitter_id, assignee_id, ai_classified, created_at) VALUES
            (''无法登录系统'',         ''从昨天开始一直提示密码错误，已尝试重置密码但未收到邮件'', ''TECHNICAL'', ''HIGH'',   ''PROCESSING'', u1, a1, TRUE,  NOW() - INTERVAL ''2 hours''),
            (''账单金额异常'',         ''本月账单显示扣费 299 元，但套餐应为 199 元'',           ''BILLING'',    ''URGENT'', ''ASSIGNED'',   u1, a1, TRUE,  NOW() - INTERVAL ''1 day''),
            (''建议增加批量导出功能'', ''希望能批量导出工单记录为 Excel'',                        ''OTHER'',      ''LOW'',    ''PENDING'',    u2, NULL, FALSE, NOW() - INTERVAL ''3 days''),
            (''客服态度投诉'',         ''昨天与客服沟通时对方态度恶劣，要求处理'',                 ''COMPLAINT'',  ''HIGH'',   ''ASSIGNED'',   u2, a2, FALSE, NOW() - INTERVAL ''4 hours''),
            (''系统响应缓慢'',         ''最近三天系统经常超时，影响正常工作'',                      ''TECHNICAL'',  ''MEDIUM'', ''RESOLVED'',   u1, a1, TRUE,  NOW() - INTERVAL ''2 days''),
            (''退款未到账'',           ''申请退款已超过 7 个工作日仍未收到'',                      ''BILLING'',    ''URGENT'', ''CLOSED'',     u2, a2, TRUE,  NOW() - INTERVAL ''5 days'');
    END IF;

    -- 捞工单 ID
    SELECT id INTO t1 FROM tickets WHERE title = ''无法登录系统'';
    SELECT id INTO t2 FROM tickets WHERE title = ''账单金额异常'';
    SELECT id INTO t3 FROM tickets WHERE title = ''建议增加批量导出功能'';
    SELECT id INTO t4 FROM tickets WHERE title = ''客服态度投诉'';
    SELECT id INTO t5 FROM tickets WHERE title = ''系统响应缓慢'';
    SELECT id INTO t6 FROM tickets WHERE title = ''退款未到账'';

    -- 回填时间戳
    UPDATE tickets SET resolved_at = NOW() - INTERVAL ''1 day''  WHERE id = t5 AND resolved_at IS NULL;
    UPDATE tickets SET resolved_at = NOW() - INTERVAL ''4 days'' WHERE id = t6 AND resolved_at IS NULL;
    UPDATE tickets SET closed_at   = NOW() - INTERVAL ''3 days'' WHERE id = t6 AND closed_at IS NULL;

    -- 回复
    IF NOT EXISTS (SELECT 1 FROM replies WHERE ticket_id = t1) THEN
        INSERT INTO replies (ticket_id, author_id, content, is_ai_draft, ai_adopted, created_at) VALUES
            (t1, a1, ''您好，已收到您的反馈。请先检查是否开启了大小写锁定，我们也在后台查看您的账号状态。'', FALSE, FALSE, NOW() - INTERVAL ''1 hour 30 minutes''),
            (t1, a1, ''经排查您的账号未被锁定，建议清除浏览器缓存后重试。如仍有问题请回复此工单。'',       FALSE, FALSE, NOW() - INTERVAL ''30 minutes''),
            (t2, a1, ''已核实您的账单，确认是系统升级导致的计费错误，已提交修正申请。'',                  FALSE, FALSE, NOW() - INTERVAL ''12 hours''),
            (t4, a2, ''非常抱歉给您带来不愉快的体验，我们已对相关客服进行谈话教育，并将加强培训。'',       FALSE, FALSE, NOW() - INTERVAL ''2 hours''),
            (t5, a1, ''我们已经定位到数据库连接池配置不足的问题，优化后响应时间已恢复正常。'',             FALSE, FALSE, NOW() - INTERVAL ''1 day 12 hours''),
            (t5, u1, ''感谢处理，今天使用确实快了很多。'',                                                FALSE, FALSE, NOW() - INTERVAL ''1 day''),
            (t6, a2, ''已为您加急处理退款，预计 1-3 个工作日内到账。'',                                  FALSE, FALSE, NOW() - INTERVAL ''5 days''),
            (t6, a2, ''退款已于今天上午到达您的账户，请查收。如有疑问请回复。'',                          FALSE, FALSE, NOW() - INTERVAL ''4 days''),
            (t6, u2, ''已收到退款，谢谢。'',                                                              FALSE, FALSE, NOW() - INTERVAL ''3 days'');
    END IF;

    -- 通知
    IF NOT EXISTS (SELECT 1 FROM notifications WHERE ticket_id = t1) THEN
        INSERT INTO notifications (user_id, type, ticket_id, message, is_read, created_at) VALUES
            (u1, ''NEW_REPLY'',      t1, ''您的工单「无法登录系统」有新的回复'',            FALSE, NOW() - INTERVAL ''1 hour 30 minutes''),
            (u1, ''NEW_REPLY'',      t1, ''您的工单「无法登录系统」有新的回复'',            FALSE, NOW() - INTERVAL ''30 minutes''),
            (u1, ''NEW_REPLY'',      t2, ''您的工单「账单金额异常」有新的回复'',            TRUE,  NOW() - INTERVAL ''12 hours''),
            (u2, ''ASSIGNED'',       t4, ''您的工单「客服态度投诉」已被分配给客服李四'',     FALSE, NOW() - INTERVAL ''4 hours''),
            (u1, ''STATUS_CHANGE'',  t6, ''您的工单「退款未到账」状态已变更为已关闭'',      TRUE,  NOW() - INTERVAL ''3 days'');
    END IF;
END
';
