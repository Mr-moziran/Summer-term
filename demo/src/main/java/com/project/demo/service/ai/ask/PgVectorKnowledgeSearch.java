package com.project.demo.service.ai.ask;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * PgVector 知识库检索实现。
 *
 * <p>复用 Spring AI VectorStore 查询向量资料，并把文档元数据转换成用户自助问答使用的知识片段。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.knowledge.provider", havingValue = "pgvector")
public class PgVectorKnowledgeSearch implements KnowledgeSearch {

	private final VectorStore vectorStore;

	public PgVectorKnowledgeSearch(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public List<KnowledgeDocument> search(String question, int topK, double similarityThreshold) {
		SearchRequest request = SearchRequest.builder()
				.query(question)
				.topK(topK)
				.similarityThresholdAll()
				.filterExpression("documentType == 'knowledge'")
				.build();
		return vectorStore.similaritySearch(request).stream()
				.map(this::toKnowledgeDocument)
				.toList();
	}

	private KnowledgeDocument toKnowledgeDocument(Document document) {
		Map<String, Object> metadata = document.getMetadata();
		return new KnowledgeDocument(
				toLong(metadata.get("knowledgeId")),
				toString(metadata.get("title"), "知识库资料"),
				toString(metadata.get("solution"), document.getText()),
				document.getScore() == null ? 0.0 : document.getScore());
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
