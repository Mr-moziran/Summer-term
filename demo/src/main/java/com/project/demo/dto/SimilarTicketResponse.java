package com.project.demo.dto;

/**
 * 相似历史工单响应 DTO，展示命中的历史工单、解决方案和相似度分数。
 */
public class SimilarTicketResponse {

	private final Long ticketId;
	private final String title;
	private final String solution;
	private final double score;

	public SimilarTicketResponse(Long ticketId, String title, String solution, double score) {
		this.ticketId = ticketId;
		this.title = title;
		this.solution = solution;
		this.score = score;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public String getTitle() {
		return title;
	}

	public String getSolution() {
		return solution;
	}

	public double getScore() {
		return score;
	}
}