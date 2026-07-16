package com.project.demo.service.ai.ticket;

import com.project.demo.domain.model.Ticket;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * PgVector 已解决工单索引实现。
 *
 * <p>把已解决工单的标题、描述、解决方案和元数据写入 Spring AI VectorStore，供后续相似检索使用。</p>
 */
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
				"documentType", "resolved-ticket",
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
