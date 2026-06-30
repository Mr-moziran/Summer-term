package com.project.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.repository.NotificationRepository;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import com.project.demo.support.TestAuthSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/db/schema.sql")
class TicketWorkflowControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void addsReplyAndCreatesNotificationForSubmitter() throws Exception {
		User submitter = saveUser("reply-user", UserRole.USER);
		User agent = saveUser("reply-agent", UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("Cannot login", "Password rejected", submitter));
		String token = authSupport.createUser(UserRole.AGENT).bearerToken();

		mockMvc.perform(post("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "authorId": %d,
						  "content": "请先尝试重置密码",
						  "aiAdopted": true
						}
						""".formatted(agent.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.ticketId").value(ticket.getId()))
				.andExpect(jsonPath("$.authorId").value(agent.getId()))
				.andExpect(jsonPath("$.content").value("请先尝试重置密码"))
				.andExpect(jsonPath("$.aiAdopted").value(true));

		mockMvc.perform(get("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].content").value("请先尝试重置密码"));

		var notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(submitter.getId());
		org.assertj.core.api.Assertions.assertThat(notifications).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.NEW_REPLY);
	}

	@Test
	void assignsTicketToAgent() throws Exception {
		User submitter = saveUser("assign-user", UserRole.USER);
		User agent = saveUser("assign-agent", UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("Need help", "Please assign", submitter));
		String token = authSupport.createUser(UserRole.ADMIN).bearerToken();

		mockMvc.perform(post("/api/tickets/{ticketId}/assign", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "assigneeId": %d
						}
						""".formatted(agent.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ASSIGNED"))
				.andExpect(jsonPath("$.assigneeId").value(agent.getId()))
				.andExpect(jsonPath("$.assigneeUsername").value(agent.getUsername()));
	}

	@Test
	void rejectsAssignForUserRole() throws Exception {
		User submitter = saveUser("assign-denied-user", UserRole.USER);
		User agent = saveUser("assign-denied-agent", UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("Need help", "Please assign", submitter));
		String token = authSupport.createUser(UserRole.USER).bearerToken();

		mockMvc.perform(post("/api/tickets/{ticketId}/assign", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "assigneeId": %d
						}
						""".formatted(agent.getId())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void updatesTicketStatusToResolved() throws Exception {
		User submitter = saveUser("status-user", UserRole.USER);
		User agent = saveUser("status-agent", UserRole.AGENT);
		Ticket ticket = new Ticket("Need status", "Resolve it", submitter);
		ticket.assignTo(agent);
		Ticket savedTicket = ticketRepository.save(ticket);
		String token = authSupport.createUser(UserRole.AGENT).bearerToken();

		mockMvc.perform(patch("/api/tickets/{ticketId}/status", savedTicket.getId())
				.header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "status": "RESOLVED"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RESOLVED"))
				.andExpect(jsonPath("$.resolvedAt").isNotEmpty());
	}

	private User saveUser(String prefix, UserRole role) {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		return userRepository.save(new User(
				prefix + "-" + suffix,
				prefix + "-" + suffix + "@example.com",
				"{bcrypt}password",
				role));
	}
}
