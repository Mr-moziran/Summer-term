package com.project.demo.ai;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketService;
import com.project.demo.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户自助问答编排服务。
 *
 * <p>服务层统一控制知识库阈值、转人工判定和工单创建，避免模型直接决定提交人、状态等敏感字段。</p>
 */
@Service
public class AskAiService {

	private static final String MEDIUM_CONFIDENCE_WARNING =
			"该回答基于现有知识库资料生成，可能不完整。如未解决，可输入“转人工”继续处理。";

	private final KnowledgeSearch knowledgeSearch;

	private final AskAiClient askAiClient;

	private final TicketService ticketService;

	private final AskAiProperties properties;

	public AskAiService(
			KnowledgeSearch knowledgeSearch,
			AskAiClient askAiClient,
			TicketService ticketService,
			AskAiProperties properties) {
		this.knowledgeSearch = knowledgeSearch;
		this.askAiClient = askAiClient;
		this.ticketService = ticketService;
		this.properties = properties;
	}

	@Transactional
	public AskAiResponse ask(User currentUser, String question) {
		String normalizedQuestion = normalizeQuestion(question);
		List<KnowledgeDocument> documents = knowledgeSearch.search(
				normalizedQuestion,
				properties.getTopK(),
				properties.getMediumThreshold());
		AskAiConfidence confidence = confidence(documents);
		if (confidence == AskAiConfidence.LOW) {
			return escalate(currentUser, normalizedQuestion, documents,
					new EscalationRequest(null, "知识库资料不足，无法可靠回答", summarize(normalizedQuestion)));
		}

		AskAiDecision decision = askAiClient.decide(normalizedQuestion, documents, confidence);
		if (decision.requiresEscalation() || hasExplicitEscalationIntent(normalizedQuestion)) {
			EscalationRequest request = decision.requiresEscalation()
					? decision.getEscalationRequest()
					: new EscalationRequest(null, "用户明确要求转人工", summarize(normalizedQuestion));
			return escalate(currentUser, normalizedQuestion, documents, request);
		}

		String answer = normalizeAnswer(decision.getAnswer());
		if (confidence == AskAiConfidence.MEDIUM) {
			return answered(AskAiResultType.ANSWERED_WITH_WARNING, answer, MEDIUM_CONFIDENCE_WARNING, documents);
		}
		return answered(AskAiResultType.ANSWERED, answer, null, documents);
	}

	private AskAiResponse answered(
			AskAiResultType resultType,
			String answer,
			String warning,
			List<KnowledgeDocument> documents) {
		return new AskAiResponse(resultType, answer, warning, true, references(documents), null);
	}

	private AskAiResponse escalate(
			User currentUser,
			String question,
			List<KnowledgeDocument> documents,
			EscalationRequest escalationRequest) {
		String summary = firstNonBlank(escalationRequest.questionSummary(), summarize(question));
		String title = abbreviate("AI未能解答：" + summary, 200);
		String description = buildEscalationDescription(question, documents, escalationRequest);
		Ticket ticket = ticketService.createTicket(currentUser, currentUser.getId(), title, description);
		return new AskAiResponse(
				AskAiResultType.ESCALATED,
				null,
				null,
				false,
				references(documents),
				EscalatedTicketResponse.from(ticket));
	}

	private String buildEscalationDescription(
			String question,
			List<KnowledgeDocument> documents,
			EscalationRequest escalationRequest) {
		StringBuilder description = new StringBuilder()
				.append("原始问题：").append(question).append("\n\n")
				.append("转人工原因：").append(firstNonBlank(escalationRequest.reason(), "AI 无法可靠回答")).append("\n\n")
				.append("知识库命中摘要：\n");
		if (documents.isEmpty()) {
			description.append("无可靠命中");
		}
		else {
			for (KnowledgeDocument document : documents) {
				description.append("- ")
						.append(document.title())
						.append("（相似度 ")
						.append(document.score())
						.append("）：")
						.append(document.content())
						.append('\n');
			}
		}
		return description.toString().trim();
	}

	private List<KnowledgeReferenceResponse> references(List<KnowledgeDocument> documents) {
		return documents.stream()
				.map(KnowledgeReferenceResponse::from)
				.toList();
	}

	private AskAiConfidence confidence(List<KnowledgeDocument> documents) {
		double maxScore = documents.stream()
				.mapToDouble(KnowledgeDocument::score)
				.max()
				.orElse(0.0);
		if (maxScore >= properties.getHighThreshold()) {
			return AskAiConfidence.HIGH;
		}
		if (maxScore >= properties.getMediumThreshold()) {
			return AskAiConfidence.MEDIUM;
		}
		return AskAiConfidence.LOW;
	}

	private boolean hasExplicitEscalationIntent(String question) {
		return question.contains("转人工")
				|| question.contains("找客服")
				|| question.contains("人工处理")
				|| question.contains("人工客服")
				|| question.contains("没解决")
				|| question.contains("我要投诉");
	}

	private String normalizeQuestion(String question) {
		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("问题不能为空");
		}
		return abbreviate(question.trim(), 1000);
	}

	private String normalizeAnswer(String answer) {
		if (answer == null || answer.isBlank()) {
			return "根据现有知识库资料，暂时无法生成更完整的回答。";
		}
		return answer.trim();
	}

	private String summarize(String question) {
		return abbreviate(question, 60);
	}

	private String firstNonBlank(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	private String abbreviate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
