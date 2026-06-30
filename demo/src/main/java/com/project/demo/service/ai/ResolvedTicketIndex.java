package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;

public interface ResolvedTicketIndex {

	void index(Ticket ticket, String solution);
}
