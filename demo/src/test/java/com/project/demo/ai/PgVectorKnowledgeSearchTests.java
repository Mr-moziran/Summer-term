package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

class PgVectorKnowledgeSearchTests {

	@Test
	void mapsVectorStoreDocumentsToKnowledgeDocuments() {
		VectorStore vectorStore = org.mockito.Mockito.mock(VectorStore.class);
		PgVectorKnowledgeSearch search = new PgVectorKnowledgeSearch(vectorStore);
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
				new Document("知识正文", Map.of(
						"ticketId", "101",
						"title", "登录问题知识",
						"solution", "点击忘记密码重置"))));

		List<KnowledgeDocument> results = search.search("忘记密码怎么办", 3, 0.70);

		ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(vectorStore).similaritySearch(requestCaptor.capture());
		SearchRequest request = requestCaptor.getValue();
		assertThat(request.getQuery()).isEqualTo("忘记密码怎么办");
		assertThat(request.getTopK()).isEqualTo(3);
		assertThat(request.getSimilarityThreshold()).isEqualTo(0.70);
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().id()).isEqualTo(101L);
		assertThat(results.getFirst().title()).isEqualTo("登录问题知识");
		assertThat(results.getFirst().content()).isEqualTo("点击忘记密码重置");
	}
}
