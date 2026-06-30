package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.index.provider", havingValue = "pgvector")
public class PgVectorResolvedTicketIndex implements ResolvedTicketIndex {

	private final VectorStore vectorStore;

	public PgVectorResolvedTicketIndex(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public void index(Ticket ticket, String solution) {
		vectorStore.add(List.of(new Document(documentText(ticket, solution), Map.of(
				"ticketId", ticket.getId(),
				"title", ticket.getTitle(),
				"solution", solution == null ? "" : solution))));
	}

	private String documentText(Ticket ticket, String solution) {
		return "标题：" + ticket.getTitle() + "\n"
				+ "描述：" + ticket.getDescription() + "\n"
				+ "解决方案：" + (solution == null ? "" : solution);
	}
}
