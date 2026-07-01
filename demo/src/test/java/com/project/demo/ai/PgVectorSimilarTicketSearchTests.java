package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.demo.ticket.Ticket;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

class PgVectorSimilarTicketSearchTests {

	@Test
	void mapsVectorStoreDocumentsToSimilarTicketContexts() {
		VectorStore vectorStore = org.mockito.Mockito.mock(VectorStore.class);
		PgVectorSimilarTicketSearch search = new PgVectorSimilarTicketSearch(vectorStore);
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
				new Document("解决方案正文", Map.of(
						"ticketId", 101L,
						"title", "历史登录问题",
						"solution", "清除Cookie后重试"))));

		List<SimilarTicketContext> results = search.search(ticket("无法登录系统", "输入正确密码后仍提示密码错误"));

		ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(vectorStore).similaritySearch(requestCaptor.capture());
		SearchRequest request = requestCaptor.getValue();
		assertThat(request.getQuery()).contains("无法登录系统");
		assertThat(request.getTopK()).isEqualTo(3);
		assertThat(request.getSimilarityThreshold()).isEqualTo(0.75);
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().ticketId()).isEqualTo(101L);
		assertThat(results.getFirst().title()).isEqualTo("历史登录问题");
		assertThat(results.getFirst().solution()).isEqualTo("清除Cookie后重试");
	}

	private Ticket ticket(String title, String description) {
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		return new Ticket(title, description, submitter);
	}
}
