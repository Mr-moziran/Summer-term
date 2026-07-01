package com.project.demo.ai;

import com.project.demo.ticket.Ticket;
import java.util.List;

/**
 * 工单 AI 客户端端口。
 *
 * <p>屏蔽本地规则实现和 DeepSeek Chat 实现差异，业务层只关心分类结果和回复草稿。</p>
 */
public interface TicketAiClient {

	TicketClassification classify(Ticket ticket);

	String draftReply(Ticket ticket, List<SimilarTicketContext> similarTickets);
}
