package com.project.demo.service.ai.ticket;

/**
 * 相似历史工单内部上下文。
 *
 * <p>该 record 是 AI Prompt 使用的中间模型，和对外响应 DTO 分离，便于服务层保留相似度等检索细节。</p>
 */
public record SimilarTicketContext(Long ticketId, String title, String solution, double score) {
}
