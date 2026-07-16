-- 企业智能客服工单系统数据库初始化脚本
-- PostgreSQL + Spring AI PgVectorStore

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'AGENT', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20),
    priority VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitter_id BIGINT NOT NULL,
    assignee_id BIGINT,
    ai_classified BOOLEAN NOT NULL DEFAULT FALSE,
    visible_to_user BOOLEAN NOT NULL DEFAULT TRUE,
    rating SMALLINT,
    rating_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    CONSTRAINT fk_ticket_submitter FOREIGN KEY (submitter_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_assignee FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_ticket_category CHECK (category IS NULL OR category IN ('TECHNICAL', 'BILLING', 'COMPLAINT', 'OTHER')),
    CONSTRAINT chk_ticket_priority CHECK (priority IS NULL OR priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_ticket_status CHECK (status IN ('PENDING', 'ASSIGNED', 'PROCESSING', 'RESOLVED', 'CLOSED')),
    CONSTRAINT chk_ticket_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5),
    CONSTRAINT chk_ticket_assignee_required CHECK (status IN ('PENDING') OR assignee_id IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS replies (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_ai_draft BOOLEAN NOT NULL DEFAULT FALSE,
    ai_adopted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reply_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    ticket_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_type CHECK (type IN ('STATUS_CHANGE', 'NEW_REPLY', 'ASSIGNED'))
);

ALTER TABLE IF EXISTS tickets
    ADD COLUMN IF NOT EXISTS visible_to_user BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

CREATE INDEX IF NOT EXISTS idx_ticket_submitter ON tickets(submitter_id);
CREATE INDEX IF NOT EXISTS idx_ticket_assignee ON tickets(assignee_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_category ON tickets(category);
CREATE INDEX IF NOT EXISTS idx_ticket_priority ON tickets(priority);
CREATE INDEX IF NOT EXISTS idx_ticket_created_at ON tickets(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ticket_queue ON tickets(status, priority, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_reply_ticket ON replies(ticket_id);
CREATE INDEX IF NOT EXISTS idx_reply_author ON replies(author_id);
CREATE INDEX IF NOT EXISTS idx_reply_created_at ON replies(created_at);

CREATE INDEX IF NOT EXISTS idx_notification_user_unread ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_ticket ON notifications(ticket_id);

COMMENT ON TABLE users IS '系统用户表，包含普通用户、客服人员和管理员';
COMMENT ON COLUMN users.id IS '用户主键';
COMMENT ON COLUMN users.username IS '用户名，系统内唯一';
COMMENT ON COLUMN users.email IS '登录邮箱，系统内唯一';
COMMENT ON COLUMN users.password IS 'BCrypt 加密后的密码';
COMMENT ON COLUMN users.role IS '用户角色：USER 普通用户，AGENT 客服人员，ADMIN 管理员';
COMMENT ON COLUMN users.status IS '账号状态：ACTIVE 启用，DISABLED 禁用';
COMMENT ON COLUMN users.avatar_url IS '用户头像地址';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '最后更新时间';

COMMENT ON TABLE tickets IS '客服工单主表，记录工单内容、状态、分类、优先级和评价';
COMMENT ON COLUMN tickets.id IS '工单主键';
COMMENT ON COLUMN tickets.title IS '工单标题';
COMMENT ON COLUMN tickets.description IS '工单详细描述';
COMMENT ON COLUMN tickets.category IS '工单分类，由 AI 或客服维护：TECHNICAL 技术问题，BILLING 账单问题，COMPLAINT 投诉，OTHER 其他';
COMMENT ON COLUMN tickets.priority IS '工单优先级：LOW 低，MEDIUM 中，HIGH 高，URGENT 紧急';
COMMENT ON COLUMN tickets.status IS '工单状态：PENDING 待分配，ASSIGNED 已分配，PROCESSING 处理中，RESOLVED 已解决，CLOSED 已关闭';
COMMENT ON COLUMN tickets.submitter_id IS '提交工单的用户 ID';
COMMENT ON COLUMN tickets.assignee_id IS '负责处理工单的客服用户 ID';
COMMENT ON COLUMN tickets.ai_classified IS '是否已完成 AI 自动分类和优先级判断';
COMMENT ON COLUMN tickets.visible_to_user IS '工单是否对提交用户可见；AI 转人工内部接管工单为 FALSE';
COMMENT ON COLUMN tickets.rating IS '用户关闭工单后的评分，1 到 5 分';
COMMENT ON COLUMN tickets.rating_comment IS '用户评价文字';
COMMENT ON COLUMN tickets.created_at IS '工单创建时间';
COMMENT ON COLUMN tickets.updated_at IS '工单最后更新时间';
COMMENT ON COLUMN tickets.resolved_at IS '客服标记已解决的时间';
COMMENT ON COLUMN tickets.closed_at IS '用户确认关闭的时间';

COMMENT ON TABLE replies IS '工单回复表，记录客服回复和 AI 草稿采纳情况';
COMMENT ON COLUMN replies.id IS '回复主键';
COMMENT ON COLUMN replies.ticket_id IS '所属工单 ID';
COMMENT ON COLUMN replies.author_id IS '回复作者用户 ID';
COMMENT ON COLUMN replies.content IS '回复内容';
COMMENT ON COLUMN replies.is_ai_draft IS '该回复是否由 AI 草稿生成';
COMMENT ON COLUMN replies.ai_adopted IS '客服是否直接采纳 AI 草稿';
COMMENT ON COLUMN replies.created_at IS '回复创建时间';

COMMENT ON TABLE notifications IS '站内通知表，用于 WebSocket 推送和未读通知查询';
COMMENT ON COLUMN notifications.id IS '通知主键';
COMMENT ON COLUMN notifications.user_id IS '接收通知的用户 ID';
COMMENT ON COLUMN notifications.type IS '通知类型：STATUS_CHANGE 状态变更，NEW_REPLY 新回复，ASSIGNED 工单分配';
COMMENT ON COLUMN notifications.ticket_id IS '关联工单 ID';
COMMENT ON COLUMN notifications.message IS '通知展示内容';
COMMENT ON COLUMN notifications.is_read IS '是否已读';
COMMENT ON COLUMN notifications.created_at IS '通知创建时间';
