package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketPriority;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "local", matchIfMissing = true)
public class LocalTicketAiClient implements TicketAiClient {

	@Override
	public TicketClassification classify(Ticket ticket) {
		String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase(Locale.ROOT);
		if (containsAny(text, "登录", "密码", "系统", "错误", "故障", "login", "password", "error")) {
			return new TicketClassification(TicketCategory.TECHNICAL, TicketPriority.HIGH, "本地规则识别为系统或登录故障");
		}
		if (containsAny(text, "账单", "扣费", "退款", "费用", "billing", "refund")) {
			return new TicketClassification(TicketCategory.BILLING, TicketPriority.MEDIUM, "本地规则识别为账单或费用问题");
		}
		if (containsAny(text, "投诉", "态度", "不满意", "complaint")) {
			return new TicketClassification(TicketCategory.COMPLAINT, TicketPriority.MEDIUM, "本地规则识别为投诉问题");
		}
		return new TicketClassification(TicketCategory.OTHER, TicketPriority.LOW, "本地规则未匹配到明确类型");
	}

	@Override
	public String draftReply(Ticket ticket, List<SimilarTicketContext> similarTickets) {
		return "您好，关于工单「" + ticket.getTitle() + "」，我们已收到您的问题："
				+ ticket.getDescription()
				+ "。建议先核对问题发生时间、操作步骤和相关截图，客服会根据这些信息继续处理。";
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}
