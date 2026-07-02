package com.project.demo.ai;

/**
 * 模型请求转人工时允许提供的受控参数。
 *
 * <p>该对象不包含提交人、处理人和状态；这些敏感字段只能由后端根据当前登录用户决定。</p>
 */
public record EscalationRequest(String suggestedTitle, String reason, String questionSummary) {
}
