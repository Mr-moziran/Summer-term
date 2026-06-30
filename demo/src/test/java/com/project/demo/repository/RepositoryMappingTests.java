package com.project.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.demo.entity.Notification;
import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Reply;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/db/schema.sql")
class RepositoryMappingTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private ReplyRepository replyRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Test
	void persistsTicketReplyAndNotificationMappings() {
		User submitter = new User("alice", "alice@example.com", "{bcrypt}password", UserRole.USER);
		User agent = new User("agent", "agent@example.com", "{bcrypt}password", UserRole.AGENT);
		userRepository.save(submitter);
		userRepository.save(agent);

		Ticket ticket = new Ticket("Cannot log in", "The system rejects my password.", submitter);
		ticket.assignTo(agent);
		Ticket savedTicket = ticketRepository.save(ticket);

		Reply reply = new Reply(savedTicket, agent, "Please reset your password.", false, false);
		replyRepository.save(reply);

		Notification notification = new Notification(
				submitter,
				NotificationType.ASSIGNED,
				savedTicket,
				"Your ticket has been assigned.");
		notificationRepository.save(notification);

		assertThat(savedTicket.getId()).isNotNull();
		assertThat(savedTicket.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
		assertThat(savedTicket.getSubmitter().getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(replyRepository.findByTicketId(savedTicket.getId())).hasSize(1);
		assertThat(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(submitter.getId())).hasSize(1);
	}
}



