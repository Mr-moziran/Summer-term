package com.project.demo.service.ai.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.demo.domain.model.Ticket;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

/**
 * PgVector 相似工单检索测试。
 */
class PgVectorSimilarTicketSearchTests {

	@Test
	void shouldSearchResolvedTicketDocumentsAndMapTheirMetadata() {
		VectorStore vectorStore = mock(VectorStore.class);
		Ticket ticket = mock(Ticket.class);
		Document document = mock(Document.class);
		when(ticket.getTitle()).thenReturn("同一订阅周期被重复扣费");
		when(ticket.getDescription()).thenReturn("同一周期出现两笔相同扣款");
		when(document.getMetadata()).thenReturn(Map.of(
				"ticketId", "244",
				"title", "同一订阅周期被重复扣费",
				"solution", "已核对为重复扣费并提交原支付渠道退款。"));
		when(document.getText()).thenReturn("历史案例正文");
		when(document.getScore()).thenReturn(0.8454);
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));

		List<SimilarTicketContext> result = new PgVectorSimilarTicketSearch(vectorStore).search(ticket);

		ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(vectorStore).similaritySearch(requestCaptor.capture());
		SearchRequest request = requestCaptor.getValue();
		assertEquals("同一订阅周期被重复扣费\n同一周期出现两笔相同扣款", request.getQuery());
		assertEquals(3, request.getTopK());
		assertEquals(0.75, request.getSimilarityThreshold());
		assertTrue(request.hasFilterExpression());
		assertEquals(new Filter.Expression(
				Filter.ExpressionType.EQ,
				new Filter.Key("documentType"),
				new Filter.Value("resolved-ticket")), request.getFilterExpression());
		assertEquals(List.of(new SimilarTicketContext(
				244L,
				"同一订阅周期被重复扣费",
				"已核对为重复扣费并提交原支付渠道退款。",
				0.8454)), result);
	}
}
