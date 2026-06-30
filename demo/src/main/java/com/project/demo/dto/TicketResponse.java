package com.project.demo.dto;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketPriority;
import com.project.demo.entity.TicketStatus;
import java.time.OffsetDateTime;

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
