package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketPriority;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "deepseek")
public class DeepSeekTicketAiClient implements TicketAiClient {

	private final ChatClient chatClient;

	public DeepSeekTicketAiClient(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@Override
	public TicketClassification classify(Ticket ticket) {
		TicketClassificationResponse response = chatClient.prompt()
				.system("你是一个客服工单分类助手。只返回JSON，不要输出额外内容。")
				.user(classificationPrompt(ticket))
				.call()
				.entity(TicketClassificationResponse.class);
		return new TicketClassification(response.category(), response.priority(), response.reason());
	}

	@Override
	public String draftReply(Ticket ticket, List<SimilarTicketContext> similarTickets) {
		String content = chatClient.prompt()
				.system("你是一名专业客服代表，请生成专业、简洁、可直接编辑的回复草稿。")
				.user(replyPrompt(ticket, similarTickets))
				.call()
				.content();
		return (content == null || content.isBlank()) ? fallbackDraft(ticket) : content.trim();
	}

	private String classificationPrompt(Ticket ticket) {
		return "请根据以下工单内容返回JSON：\n"
				+ "{\"category\":\"TECHNICAL|BILLING|COMPLAINT|OTHER\","
				+ "\"priority\":\"LOW|MEDIUM|HIGH|URGENT\",\"reason\":\"一句话说明分类依据\"}\n\n"
				+ "分类规则：TECHNICAL=功能故障、无法登录、系统错误；BILLING=费用、退款、账单；"
				+ "COMPLAINT=服务态度或处理结果投诉；OTHER=其他。\n"
				+ "优先级：URGENT=服务完全不可用；HIGH=主要功能受影响；MEDIUM=部分功能受影响；LOW=咨询类。\n\n"
				+ "工单标题：" + ticket.getTitle() + "\n"
				+ "工单描述：" + ticket.getDescription();
	}

	private String replyPrompt(Ticket ticket, List<SimilarTicketContext> similarTickets) {
		StringBuilder prompt = new StringBuilder()
				.append("【当前工单】\n")
				.append("类型：").append(ticket.getCategory()).append('\n')
				.append("优先级：").append(ticket.getPriority()).append('\n')
				.append("标题：").append(ticket.getTitle()).append('\n')
				.append("描述：").append(ticket.getDescription()).append("\n\n")
				.append("【历史相似案例】\n");
		if (similarTickets.isEmpty()) {
			prompt.append("无\n");
		} else {
			for (SimilarTicketContext similarTicket : similarTickets) {
				prompt.append("- ")
						.append(similarTicket.title())
						.append("，解决方案：")
						.append(similarTicket.solution())
						.append("，相似度：")
						.append(similarTicket.score())
						.append('\n');
			}
		}
		return prompt.append("\n请生成客服回复草稿，语气专业、明确下一步处理建议。")
				.toString();
	}

	private String fallbackDraft(Ticket ticket) {
		return "您好，关于工单「" + ticket.getTitle() + "」，我们已收到您的问题："
				+ ticket.getDescription()
				+ "。建议先核对问题发生时间、操作步骤和相关截图，客服会根据这些信息继续处理。";
	}

	private record TicketClassificationResponse(TicketCategory category, TicketPriority priority, String reason) {
	}
}
