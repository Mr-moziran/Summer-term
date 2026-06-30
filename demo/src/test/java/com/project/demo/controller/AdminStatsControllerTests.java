package com.project.demo.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.entity.Reply;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.repository.ReplyRepository;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import com.project.demo.support.TestAuthSupport;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
		@Sql(scripts = "/db/schema.sql"),
		@Sql(statements = {
				"DELETE FROM notification",
				"DELETE FROM reply",
				"DELETE FROM ticket",
				"DELETE FROM users"
		})
})
class AdminStatsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private ReplyRepository replyRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void adminGetsDashboardStats() throws Exception {
		var admin = authSupport.createUser(UserRole.ADMIN);
		User submitter = saveUser("stats-user", UserRole.USER);
		User agent = saveUser("stats-agent", UserRole.AGENT);
		OffsetDateTime now = OffsetDateTime.now();

		Ticket pendingToday = ticketRepository.save(new Ticket("Pending", "Waiting", submitter));
		setTicketFields(pendingToday, "TECHNICAL", "PENDING", now.minusHours(2), null);

		Ticket resolvedToday = ticketRepository.save(new Ticket("Resolved", "Has reply", submitter));
		resolvedToday.assignTo(agent);
		resolvedToday.changeStatus(TicketStatus.RESOLVED);
		Ticket savedResolvedToday = ticketRepository.save(resolvedToday);
		setTicketFields(savedResolvedToday, "BILLING", "RESOLVED", now.minusHours(1), agent.getId());
		Reply adoptedReply = replyRepository.save(new Reply(savedResolvedToday, agent, "已处理", false, true));
		setReplyCreatedAt(adoptedReply, now.minusMinutes(40));

		Ticket oldTicket = ticketRepository.save(new Ticket("Old", "Yesterday", submitter));
		oldTicket.assignTo(agent);
		Ticket savedOldTicket = ticketRepository.save(oldTicket);
		setTicketFields(savedOldTicket, "TECHNICAL", "ASSIGNED", now.minusDays(1), agent.getId());
		Reply manualReply = replyRepository.save(new Reply(savedOldTicket, agent, "人工处理", false, false));
		setReplyCreatedAt(manualReply, now.minusDays(1).plusMinutes(20));

		mockMvc.perform(get("/api/admin/stats")
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.todayTotal").value(2))
				.andExpect(jsonPath("$.pendingCount").value(1))
				.andExpect(jsonPath("$.avgResponseMinutes").value(closeTo(20.0, 0.1)))
				.andExpect(jsonPath("$.categoryDistribution.TECHNICAL").value(closeTo(0.67, 0.01)))
				.andExpect(jsonPath("$.categoryDistribution.BILLING").value(closeTo(0.33, 0.01)))
				.andExpect(jsonPath("$.aiAdoptionRate").value(closeTo(0.5, 0.01)));
	}

	@Test
	void adminGetsAgentPerformanceStats() throws Exception {
		var admin = authSupport.createUser(UserRole.ADMIN);
		User submitter = saveUser("agent-stats-user", UserRole.USER);
		User firstAgent = saveUser("agent-stats-a", UserRole.AGENT);
		User secondAgent = saveUser("agent-stats-b", UserRole.AGENT);
		OffsetDateTime now = OffsetDateTime.now();

		Ticket firstResolved = ticketRepository.save(new Ticket("First resolved", "Resolved by first agent", submitter));
		firstResolved.assignTo(firstAgent);
		firstResolved.changeStatus(TicketStatus.RESOLVED);
		Ticket savedFirstResolved = ticketRepository.save(firstResolved);
		setTicketFields(savedFirstResolved, "TECHNICAL", "RESOLVED", now.minusHours(2), firstAgent.getId());
		Reply firstAdoptedReply = replyRepository.save(new Reply(savedFirstResolved, firstAgent, "AI answer", false, true));
		setReplyCreatedAt(firstAdoptedReply, now.minusMinutes(90));

		Ticket firstAssigned = ticketRepository.save(new Ticket("First assigned", "Still processing", submitter));
		firstAssigned.assignTo(firstAgent);
		Ticket savedFirstAssigned = ticketRepository.save(firstAssigned);
		setTicketFields(savedFirstAssigned, "BILLING", "ASSIGNED", now.minusHours(1), firstAgent.getId());
		Reply firstManualReply = replyRepository.save(new Reply(savedFirstAssigned, firstAgent, "Manual answer", false, false));
		setReplyCreatedAt(firstManualReply, now.minusMinutes(45));

		Ticket secondResolved = ticketRepository.save(new Ticket("Second resolved", "Resolved by second agent", submitter));
		secondResolved.assignTo(secondAgent);
		secondResolved.changeStatus(TicketStatus.RESOLVED);
		Ticket savedSecondResolved = ticketRepository.save(secondResolved);
		setTicketFields(savedSecondResolved, "COMPLAINT", "RESOLVED", now.minusHours(3), secondAgent.getId());
		Reply secondReply = replyRepository.save(new Reply(savedSecondResolved, secondAgent, "Second answer", false, false));
		setReplyCreatedAt(secondReply, now.minusHours(2));

		mockMvc.perform(get("/api/admin/stats/agents")
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].agentId").value(firstAgent.getId()))
				.andExpect(jsonPath("$[0].username").value(firstAgent.getUsername()))
				.andExpect(jsonPath("$[0].assignedCount").value(2))
				.andExpect(jsonPath("$[0].resolvedCount").value(1))
				.andExpect(jsonPath("$[0].replyCount").value(2))
				.andExpect(jsonPath("$[0].aiAdoptionRate").value(closeTo(0.5, 0.01)))
				.andExpect(jsonPath("$[0].avgResponseMinutes").value(closeTo(22.5, 0.1)))
				.andExpect(jsonPath("$[1].agentId").value(secondAgent.getId()))
				.andExpect(jsonPath("$[1].assignedCount").value(1))
				.andExpect(jsonPath("$[1].resolvedCount").value(1))
				.andExpect(jsonPath("$[1].replyCount").value(1))
				.andExpect(jsonPath("$[1].aiAdoptionRate").value(closeTo(0.0, 0.01)))
				.andExpect(jsonPath("$[1].avgResponseMinutes").value(closeTo(60.0, 0.1)));
	}

	@Test
	void userCannotGetDashboardStats() throws Exception {
		var user = authSupport.createUser(UserRole.USER);

		mockMvc.perform(get("/api/admin/stats")
				.header(HttpHeaders.AUTHORIZATION, user.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	private void setTicketFields(Ticket ticket, String category, String status, OffsetDateTime createdAt, Long assigneeId) {
		jdbcTemplate.update("""
				UPDATE ticket
				SET category = ?, status = ?, created_at = ?, updated_at = ?, assignee_id = ?
				WHERE id = ?
				""", category, status, createdAt, createdAt, assigneeId, ticket.getId());
	}

	private void setReplyCreatedAt(Reply reply, OffsetDateTime createdAt) {
		jdbcTemplate.update("UPDATE reply SET created_at = ? WHERE id = ?", createdAt, reply.getId());
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
