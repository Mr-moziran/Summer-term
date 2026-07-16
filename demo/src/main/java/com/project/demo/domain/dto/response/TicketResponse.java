package com.project.demo.domain.dto.response;

import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.enums.TicketCategory;
import com.project.demo.domain.enums.TicketPriority;
import com.project.demo.domain.enums.TicketStatus;

import java.time.OffsetDateTime;

/**
 * 工单响应 DTO，聚合工单主体信息、提交人、处理人、评分和时间字段。
 */
public class TicketResponse {

	private final Long id;
	private final String title;
	private final String description;
	private final TicketCategory category;
	private final TicketPriority priority;
	private final TicketStatus status;
	private final Long submitterId;
	private final String submitterUsername;
	private final Long assigneeId;
	private final String assigneeUsername;
	private final boolean aiClassified;
	private final Short rating;
	private final String ratingComment;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	private final OffsetDateTime resolvedAt;
	private final OffsetDateTime closedAt;

	private TicketResponse(Ticket ticket) {
		this.id = ticket.getId();
		this.title = ticket.getTitle();
		this.description = ticket.getDescription();
		this.category = ticket.getCategory();
		this.priority = ticket.getPriority();
		this.status = ticket.getStatus();
		this.submitterId = ticket.getSubmitter().getId();
		this.submitterUsername = ticket.getSubmitter().getUsername();
		this.assigneeId = ticket.getAssignee() == null ? null : ticket.getAssignee().getId();
		this.assigneeUsername = ticket.getAssignee() == null ? null : ticket.getAssignee().getUsername();
		this.aiClassified = ticket.isAiClassified();
		this.rating = ticket.getRating();
		this.ratingComment = ticket.getRatingComment();
		this.createdAt = ticket.getCreatedAt();
		this.updatedAt = ticket.getUpdatedAt();
		this.resolvedAt = ticket.getResolvedAt();
		this.closedAt = ticket.getClosedAt();
	}

	public static TicketResponse from(Ticket ticket) {
		return new TicketResponse(ticket);
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TicketCategory getCategory() {
		return category;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public Long getSubmitterId() {
		return submitterId;
	}

	public String getSubmitterUsername() {
		return submitterUsername;
	}

	public Long getAssigneeId() {
		return assigneeId;
	}

	public String getAssigneeUsername() {
		return assigneeUsername;
	}

	public boolean isAiClassified() {
		return aiClassified;
	}

	public Short getRating() {
		return rating;
	}

	public String getRatingComment() {
		return ratingComment;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public OffsetDateTime getResolvedAt() {
		return resolvedAt;
	}

	public OffsetDateTime getClosedAt() {
		return closedAt;
	}
}
