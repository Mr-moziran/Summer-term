package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketService;
import com.project.demo.ticket.TicketStatus;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;

class AskAiServiceTests {

	private final User currentUser = user(7L);

	private final CapturingKnowledgeSearch knowledgeSearch = new CapturingKnowledgeSearch();

	private final CapturingAskAiClient askAiClient = new CapturingAskAiClient();

	private final TicketService ticketService = org.mockito.Mockito.mock(TicketService.class);

	private final AskAiProperties properties = new AskAiProperties(0.82, 0.70, 3);

	private final AskAiService service = new AskAiService(knowledgeSearch, askAiClient, ticketService, properties);

	@Test
	void highConfidenceKnowledgeReturnsAnswerWithoutWarning() {
		knowledgeSearch.documents = List.of(new KnowledgeDocument(11L, "登录帮助", "点击忘记密码重置。", 0.91));
		askAiClient.decision = AskAiDecision.answer("请点击忘记密码完成重置。");

		AskAiResponse response = service.ask(currentUser, "忘记密码怎么办");

		assertThat(response.getResultType()).isEqualTo(AskAiResultType.ANSWERED);
		assertThat(response.getAnswer()).isEqualTo("请点击忘记密码完成重置。");
		assertThat(response.getWarning()).isNull();
		assertThat(response.isCanEscalate()).isTrue();
		assertThat(response.getReferences()).hasSize(1);
		assertThat(response.getReferences().getFirst().getTitle()).isEqualTo("登录帮助");
		assertThat(askAiClient.confidence()).isEqualTo(AskAiConfidence.HIGH);
		verifyNoInteractions(ticketService);
	}

	@Test
	void mediumConfidenceKnowledgeReturnsAnswerWithWarning() {
		knowledgeSearch.documents = List.of(new KnowledgeDocument(12L, "缓存处理", "清理浏览器缓存后重试。", 0.75));
		askAiClient.decision = AskAiDecision.answer("请清理浏览器缓存后重试。");

		AskAiResponse response = service.ask(currentUser, "页面一直报错");

		assertThat(response.getResultType()).isEqualTo(AskAiResultType.ANSWERED_WITH_WARNING);
		assertThat(response.getAnswer()).isEqualTo("请清理浏览器缓存后重试。");
		assertThat(response.getWarning()).contains("可能不完整");
		assertThat(response.isCanEscalate()).isTrue();
		verifyNoInteractions(ticketService);
	}

	@Test
	void lowConfidenceKnowledgeCreatesHumanTicket() {
		knowledgeSearch.documents = List.of(new KnowledgeDocument(13L, "弱相关资料", "这是一条弱相关资料。", 0.62));
		Ticket createdTicket = ticket("AI未能解答：未知问题");
		when(ticketService.createTicket(eq(currentUser), eq(currentUser.getId()), any(), any()))
				.thenReturn(createdTicket);

		AskAiResponse response = service.ask(currentUser, "这是一个未知问题");

		assertThat(response.getResultType()).isEqualTo(AskAiResultType.ESCALATED);
		assertThat(response.getTicket().getId()).isEqualTo(101L);
		assertThat(response.getTicket().getStatus()).isEqualTo(TicketStatus.PENDING);
		assertThat(response.isCanEscalate()).isFalse();
		assertThat(response.getAnswer()).isNull();
		verifyNoInteractionsWithClientAnswer();
		verify(ticketService).createTicket(eq(currentUser), eq(currentUser.getId()), any(), any());
	}

	@Test
	void explicitTransferRequestCreatesHumanTicketEvenWhenKnowledgeMatches() {
		knowledgeSearch.documents = List.of(new KnowledgeDocument(14L, "登录帮助", "点击忘记密码重置。", 0.91));
		askAiClient.decision = AskAiDecision.escalate(new EscalationRequest(
				"登录问题需要人工协助",
				"用户明确要求转人工",
				"忘记密码无法登录"));
		Ticket createdTicket = ticket("AI未能解答：忘记密码无法登录");
		when(ticketService.createTicket(eq(currentUser), eq(currentUser.getId()), any(), any()))
				.thenReturn(createdTicket);

		AskAiResponse response = service.ask(currentUser, "我忘记密码了，转人工处理");

		assertThat(response.getResultType()).isEqualTo(AskAiResultType.ESCALATED);
		assertThat(response.getTicket().getTitle()).isEqualTo("AI未能解答：忘记密码无法登录");
		verify(ticketService).createTicket(eq(currentUser), eq(currentUser.getId()), any(), any());
	}

	@Test
	void modelEscalationCannotOverrideCurrentSubmitter() {
		knowledgeSearch.documents = List.of(new KnowledgeDocument(15L, "弱相关资料", "请联系客服。", 0.72));
		askAiClient.decision = AskAiDecision.escalate(new EscalationRequest(
				"恶意指定提交人",
				"模型请求建单",
				"模型不能指定提交人"));
		Ticket createdTicket = ticket("AI未能解答：模型不能指定提交人");
		when(ticketService.createTicket(eq(currentUser), eq(currentUser.getId()), any(), any()))
				.thenReturn(createdTicket);

		service.ask(currentUser, "这个问题需要人工");

		verify(ticketService).createTicket(eq(currentUser), eq(7L), any(), any());
	}

	private void verifyNoInteractionsWithClientAnswer() {
		assertThat(askAiClient.callCount).isZero();
	}

	private User user(Long id) {
		User user = new User("ask-user", "ask-user@example.com", "{bcrypt}password", UserRole.USER);
		setId(user, id);
		return user;
	}

	private Ticket ticket(String title) {
		Ticket ticket = new Ticket(title, "description", currentUser);
		setId(ticket, 101L);
		return ticket;
	}

	private void setId(Object entity, Long id) {
		try {
			java.lang.reflect.Field field = entity.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(entity, id);
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final class CapturingKnowledgeSearch implements KnowledgeSearch {

		private List<KnowledgeDocument> documents = List.of();

		@Override
		public List<KnowledgeDocument> search(String question, int topK, double similarityThreshold) {
			return documents;
		}
	}

	private static final class CapturingAskAiClient implements AskAiClient {

		private AskAiDecision decision = AskAiDecision.answer("默认回答");

		private AskAiConfidence confidence;

		private int callCount;

		@Override
		public AskAiDecision decide(String question, List<KnowledgeDocument> documents, AskAiConfidence confidence) {
			this.callCount++;
			this.confidence = confidence;
			return decision;
		}

		private AskAiConfidence confidence() {
			return confidence;
		}
	}
}
