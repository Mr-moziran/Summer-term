package com.project.demo.ai;

import java.util.List;

/**
 * 用户自助问答响应。
 */
public class AskAiResponse {

	private final AskAiResultType resultType;

	private final String answer;

	private final String warning;

	private final boolean canEscalate;

	private final List<KnowledgeReferenceResponse> references;

	private final EscalatedTicketResponse ticket;

	public AskAiResponse(
			AskAiResultType resultType,
			String answer,
			String warning,
			boolean canEscalate,
			List<KnowledgeReferenceResponse> references,
			EscalatedTicketResponse ticket) {
		this.resultType = resultType;
		this.answer = answer;
		this.warning = warning;
		this.canEscalate = canEscalate;
		this.references = List.copyOf(references);
		this.ticket = ticket;
	}

	public AskAiResultType getResultType() {
		return resultType;
	}

	public String getAnswer() {
		return answer;
	}

	public String getWarning() {
		return warning;
	}

	public boolean isCanEscalate() {
		return canEscalate;
	}

	public List<KnowledgeReferenceResponse> getReferences() {
		return references;
	}

	public EscalatedTicketResponse getTicket() {
		return ticket;
	}
}
