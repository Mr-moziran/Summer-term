package com.project.demo.ai;

import com.project.demo.ticket.TicketCategory;
import com.project.demo.ticket.TicketPriority;

/**
 * AI 分类结果。
 *
 * <p>包含分类、优先级和模型给出的简短理由；当前业务只持久化分类和优先级。</p>
 */
public record TicketClassification(TicketCategory category, TicketPriority priority, String reason) {
}
