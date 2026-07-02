package com.project.demo.ai;

/**
 * 自助问答返回给前端的知识来源。
 */
public class KnowledgeReferenceResponse {

	private final String title;

	private final double score;

	public KnowledgeReferenceResponse(String title, double score) {
		this.title = title;
		this.score = score;
	}

	public String getTitle() {
		return title;
	}

	public double getScore() {
		return score;
	}

	public static KnowledgeReferenceResponse from(KnowledgeDocument document) {
		return new KnowledgeReferenceResponse(document.title(), document.score());
	}
}
