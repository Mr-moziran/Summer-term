package com.project.demo.dto;

import com.project.demo.entity.Reply;
import java.time.OffsetDateTime;

public class ReplyResponse {

	private final Long id;
	private final Long ticketId;
	private final Long authorId;
	private final String authorUsername;
	private final String content;
	private final boolean aiDraft;
	private final boolean aiAdopted;
	private final OffsetDateTime createdAt;

	private ReplyResponse(Reply reply) {
		this.id = reply.getId();
		this.ticketId = reply.getTicket().getId();
		this.authorId = reply.getAuthor().getId();
		this.authorUsername = reply.getAuthor().getUsername();
		this.content = reply.getContent();
		this.aiDraft = reply.isAiDraft();
		this.aiAdopted = reply.isAiAdopted();
		this.createdAt = reply.getCreatedAt();
	}

	public static ReplyResponse from(Reply reply) {
		return new ReplyResponse(reply);
	}

	public Long getId() {
		return id;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public String getContent() {
		return content;
	}

	public boolean isAiDraft() {
		return aiDraft;
	}

	public boolean isAiAdopted() {
		return aiAdopted;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
