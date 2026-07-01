package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketCategory;
import com.project.demo.ticket.TicketPriority;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import com.project.demo.ticket.TicketRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTests {

	@Test
	void suggestionIncludesSimilarTicketsFromSearch() {
		TicketRepository ticketRepository = org.mockito.Mockito.mock(TicketRepository.class);
		CapturingTicketAiClient ticketAiClient = new CapturingTicketAiClient();
		SimilarTicketSearch similarTicketSearch = ticket -> List.of(
				new SimilarTicketContext(101L, "历史登录问题", "清除Cookie后重试", 0.91));
		AiSuggestionService service = new AiSuggestionService(ticketRepository, ticketAiClient, similarTicketSearch);
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		User agent = new User("agent", "agent@example.com", "{bcrypt}password", UserRole.AGENT);
		Ticket ticket = new Ticket("无法登录系统", "输入正确密码后仍提示密码错误", submitter);
		ticket.assignTo(agent);
		setId(ticket, 1L);
		setId(agent, 2L);

		when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

		AiSuggestionResponse response = service.suggest(agent, 1L);

		assertThat(ticketAiClient.similarTickets).hasSize(1);
		assertThat(response.getSimilarTickets()).hasSize(1);
		assertThat(response.getSimilarTickets().getFirst().getTicketId()).isEqualTo(101L);
		assertThat(response.getSimilarTickets().getFirst().getTitle()).isEqualTo("历史登录问题");
		assertThat(response.getSimilarTickets().getFirst().getSolution()).isEqualTo("清除Cookie后重试");
		assertThat(response.getSimilarTickets().getFirst().getScore()).isEqualTo(0.91);
	}

	private void setId(Object entity, Long id) {
		try {
			java.lang.reflect.Field field = entity.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(entity, id);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final class CapturingTicketAiClient implements TicketAiClient {

		private List<SimilarTicketContext> similarTickets = List.of();

		@Override
		public TicketClassification classify(Ticket ticket) {
			return new TicketClassification(TicketCategory.TECHNICAL, TicketPriority.HIGH, "测试分类");
		}

		@Override
		public String draftReply(Ticket ticket, List<SimilarTicketContext> similarTickets) {
			this.similarTickets = similarTickets;
			return "测试草稿";
		}
	}
}
