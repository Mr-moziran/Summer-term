package com.project.demo.domain.dto.response;

import java.util.List;

/**
 * AI 建议响应 DTO，包含回复草稿和相似历史工单列表。
 */
public class AiSuggestionResponse {

	private final String draft;
	private final List<SimilarTicketResponse> similarTickets;

	public AiSuggestionResponse(String draft, List<SimilarTicketResponse> similarTickets) {
		this.draft = draft;
		this.similarTickets = similarTickets;
	}

	public String getDraft() {
		return draft;
	}

	public List<SimilarTicketResponse> getSimilarTickets() {
		return similarTickets;
	}
}