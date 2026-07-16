package com.project.demo.service.ai.ticket;

import com.project.demo.domain.model.Ticket;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * PgVector 相似工单检索实现。
 *
 * <p>使用 Spring AI VectorStore 生成查询向量并在 vector_store 中做相似度检索，再把文档元数据还原成业务上下文。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.similarity.provider", havingValue = "pgvector")
public class PgVectorSimilarTicketSearch implements SimilarTicketSearch {

	private static final int TOP_K = 3;
	private static final double SIMILARITY_THRESHOLD = 0.75;

	private final VectorStore vectorStore;

	public PgVectorSimilarTicketSearch(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public List<SimilarTicketContext> search(Ticket ticket) {
		SearchRequest request = SearchRequest.builder()
				.query(ticket.getTitle() + "\n" + ticket.getDescription())
				.topK(TOP_K)
				.similarityThreshold(SIMILARITY_THRESHOLD)
				.filterExpression("documentType == 'resolved-ticket'")
				.build();
		return vectorStore.similaritySearch(request).stream()
				.map(this::toContext)
				.toList();
	}

	/**
	 * 将 Spring AI Document 转换为业务上下文。
	 *
	 * <p>metadata 来自索引写入阶段，包含 ticketId/title/solution；缺失时使用文档正文作为兜底解决方案。</p>
	 */
	private SimilarTicketContext toContext(Document document) {
		Map<String, Object> metadata = document.getMetadata();
		Long ticketId = toLong(metadata.get("ticketId"));
		String title = toString(metadata.get("title"), "历史工单");
		String solution = toString(metadata.get("solution"), document.getText());
		Double score = document.getScore();
		return new SimilarTicketContext(ticketId, title, solution, score == null ? 0.0 : score);
	}

	private Long toLong(Object value) {
		if (value instanceof Number number) {
			return number.longValue();
		}
		if (value instanceof String text && !text.isBlank()) {
			return Long.parseLong(text);
		}
		return null;
	}

	private String toString(Object value, String fallback) {
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		return fallback == null ? "" : fallback;
	}
}
