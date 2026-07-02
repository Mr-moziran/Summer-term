package com.project.demo.ai;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketStatus;

/**
 * 自助问答转人工后返回的工单摘要。
 */
public class EscalatedTicketResponse {

	private final Long id;

	private final TicketStatus status;

	private final String title;

	public EscalatedTicketResponse(Long id, TicketStatus status, String title) {
		this.id = id;
		this.status = status;
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public String getTitle() {
		return title;
	}

	public static EscalatedTicketResponse from(Ticket ticket) {
		return new EscalatedTicketResponse(ticket.getId(), ticket.getStatus(), ticket.getTitle());
	}
}
