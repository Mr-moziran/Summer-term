package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketCategory;
import com.project.demo.ticket.TicketPriority;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import com.project.demo.ticket.TicketRepository;
import com.project.demo.user.UserRepository;
import com.project.demo.support.TestAuthSupport;
import com.project.demo.support.TestAuthSupport.AuthUser;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/db/schema.sql")
class AiSuggestionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void assignedAgentGetsSuggestionAndClassifiesTicket() throws Exception {
		User submitter = saveUser("ai-submit", UserRole.USER);
		AuthUser agent = authSupport.createUser(UserRole.AGENT);
		Ticket ticket = new Ticket("无法登录系统", "输入正确密码后仍提示密码错误", submitter);
		ticket.assignTo(agent.user());
		Ticket savedTicket = ticketRepository.save(ticket);

		mockMvc.perform(get("/api/ai/suggest/{ticketId}", savedTicket.getId())
				.header(HttpHeaders.AUTHORIZATION, agent.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.draft", containsString("无法登录系统")))
				.andExpect(jsonPath("$.similarTickets", hasSize(0)));

		Ticket classifiedTicket = ticketRepository.findById(savedTicket.getId()).orElseThrow();
		assertThat(classifiedTicket.getCategory()).isEqualTo(TicketCategory.TECHNICAL);
		assertThat(classifiedTicket.getPriority()).isEqualTo(TicketPriority.HIGH);
		assertThat(classifiedTicket.isAiClassified()).isTrue();
	}

	@Test
	void adminGetsSuggestionForAnyTicket() throws Exception {
		User submitter = saveUser("ai-admin-submit", UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("账单问题", "扣费金额不正确", submitter));
		AuthUser admin = authSupport.createUser(UserRole.ADMIN);

		mockMvc.perform(get("/api/ai/suggest/{ticketId}", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.draft", containsString("账单问题")))
				.andExpect(jsonPath("$.similarTickets", hasSize(0)));
	}

	@Test
	void unassignedAgentCannotGetSuggestion() throws Exception {
		User submitter = saveUser("ai-denied-submit", UserRole.USER);
		User assignedAgent = saveUser("ai-denied-agent", UserRole.AGENT);
		AuthUser otherAgent = authSupport.createUser(UserRole.AGENT);
		Ticket ticket = new Ticket("无法登录系统", "输入正确密码后仍提示密码错误", submitter);
		ticket.assignTo(assignedAgent);
		Ticket savedTicket = ticketRepository.save(ticket);

		mockMvc.perform(get("/api/ai/suggest/{ticketId}", savedTicket.getId())
				.header(HttpHeaders.AUTHORIZATION, otherAgent.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void userCannotGetSuggestion() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("无法登录系统", "输入正确密码后仍提示密码错误", submitter.user()));

		mockMvc.perform(get("/api/ai/suggest/{ticketId}", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
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