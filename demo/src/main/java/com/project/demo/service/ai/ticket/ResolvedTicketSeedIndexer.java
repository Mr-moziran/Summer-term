package com.project.demo.service.ai.ticket;

import com.project.demo.domain.enums.TicketStatus;
import com.project.demo.domain.model.Reply;
import com.project.demo.domain.model.Ticket;
import com.project.demo.repository.ReplyRepository;
import com.project.demo.repository.TicketRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 把演示数据库中已解决或已关闭的历史工单重建到相似工单索引。
 *
 * <p>仅在显式开启开关时执行，重建范围不会影响 FAQ 知识库文档。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.index.seed.enabled", havingValue = "true")
public class ResolvedTicketSeedIndexer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ResolvedTicketSeedIndexer.class);

	private final TicketRepository ticketRepository;

	private final ReplyRepository replyRepository;

	private final VectorStore vectorStore;

	public ResolvedTicketSeedIndexer(
			TicketRepository ticketRepository,
			ReplyRepository replyRepository,
			VectorStore vectorStore) {
		this.ticketRepository = ticketRepository;
		this.replyRepository = replyRepository;
		this.vectorStore = vectorStore;
	}

	@Override
	public void run(ApplicationArguments args) {
		List<Document> documents = ticketRepository.findAll().stream()
				.filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED
						|| ticket.getStatus() == TicketStatus.CLOSED)
				.map(this::toDocument)
				.toList();
		vectorStore.delete("documentType == 'resolved-ticket'");
		if (!documents.isEmpty()) {
			vectorStore.add(documents);
		}
		log.info("已重建 {} 条已解决工单向量索引", documents.size());
	}

	private Document toDocument(Ticket ticket) {
		String solution = latestReplyContent(ticket.getId());
		return new Document("标题：" + ticket.getTitle() + "\n"
				+ "描述：" + ticket.getDescription() + "\n"
				+ "解决方案：" + solution, Map.of(
						"documentType", "resolved-ticket",
						"ticketId", ticket.getId(),
						"title", ticket.getTitle(),
						"solution", solution));
	}

	private String latestReplyContent(Long ticketId) {
		List<Reply> replies = replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
		return replies.isEmpty() ? "" : replies.getLast().getContent();
	}
}
