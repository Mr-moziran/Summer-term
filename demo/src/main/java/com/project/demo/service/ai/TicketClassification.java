package com.project.demo.service.ai;

import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketPriority;

/**
 * AI 分类结果。
 *
 * <p>包含分类、优先级和模型给出的简短理由；当前业务只持久化分类和优先级。</p>
 */
public record TicketClassification(TicketCategory category, TicketPriority priority, String reason) {
}
