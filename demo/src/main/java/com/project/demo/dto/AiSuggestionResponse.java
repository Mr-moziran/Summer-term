package com.project.demo.dto;

import java.util.List;

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