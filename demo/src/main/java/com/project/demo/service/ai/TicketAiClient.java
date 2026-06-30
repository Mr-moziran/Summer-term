package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;

public interface TicketAiClient {

	TicketClassification classify(Ticket ticket);

	String draftReply(Ticket ticket, List<SimilarTicketContext> similarTickets);
}
