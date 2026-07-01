package com.project.demo.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.project.demo.reply.Reply;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import com.project.demo.notification.NotificationService;
import com.project.demo.reply.ReplyRepository;
import com.project.demo.user.UserRepository;
import com.project.demo.ai.ResolvedTicketIndex;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TicketWorkflowServiceTests {

	@Test
	void indexesResolvedTicketWithLatestReplySolution() {
		TicketRepository ticketRepository = org.mockito.Mockito.mock(TicketRepository.class);
		ReplyRepository replyRepository = org.mockito.Mockito.mock(ReplyRepository.class);
		UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
		NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
		CapturingResolvedTicketIndex resolvedTicketIndex = new CapturingResolvedTicketIndex();
		TicketWorkflowService service = new TicketWorkflowService(
				ticketRepository,
				replyRepository,
				userRepository,
				notificationService,
				resolvedTicketIndex);
		User submitter = user("submitter", UserRole.USER, 1L);
		User agent = user("agent", UserRole.AGENT, 2L);
		Ticket ticket = new Ticket("无法登录系统", "输入正确密码后仍提示密码错误", submitter);
		ticket.assignTo(agent);
		setId(ticket, 10L);
		Reply firstReply = new Reply(ticket, agent, "请先重置密码", false, false);
		Reply latestReply = new Reply(ticket, agent, "清除Cookie后重试", false, false);
		when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
		when(replyRepository.findByTicketIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(firstReply, latestReply));

		service.updateStatus(agent, 10L, TicketStatus.RESOLVED);

		assertThat(resolvedTicketIndex.ticket).isSameAs(ticket);
		assertThat(resolvedTicketIndex.solution).isEqualTo("清除Cookie后重试");
	}

	private User user(String username, UserRole role, Long id) {
		User user = new User(username, username + "@example.com", "{bcrypt}password", role);
		setId(user, id);
		return user;
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

	private static final class CapturingResolvedTicketIndex implements ResolvedTicketIndex {

		private Ticket ticket;
		private String solution;

		@Override
		public void index(Ticket ticket, String solution) {
			this.ticket = ticket;
			this.solution = solution;
		}
	}
}
