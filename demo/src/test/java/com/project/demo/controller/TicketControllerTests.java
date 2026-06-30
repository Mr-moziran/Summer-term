package com.project.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
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
class TicketControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void createsTicketForExistingSubmitter() throws Exception {
		User submitter = saveUser("create-user");
		String token = authSupport.createUser(UserRole.USER).bearerToken();

		mockMvc.perform(post("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "submitterId": %d,
						  "title": "无法登录系统",
						  "description": "昨天开始一直提示密码错误"
						}
						""".formatted(submitter.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("无法登录系统"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.submitterId").value(submitter.getId()))
				.andExpect(jsonPath("$.aiClassified").value(false));
	}

	@Test
	void listsTicketsWithSubmitterFilter() throws Exception {
		User alice = saveUser("list-alice");
		User bob = saveUser("list-bob");
		ticketRepository.save(new Ticket("Alice ticket", "Alice description", alice));
		ticketRepository.save(new Ticket("Bob ticket", "Bob description", bob));
		String token = authSupport.createUser(UserRole.USER).bearerToken();

		mockMvc.perform(get("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, token)
				.param("submitterId", alice.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("Alice ticket"))
				.andExpect(jsonPath("$.content[0].submitterId").value(alice.getId()));
	}

	@Test
	void rejectsTicketListWithoutToken() throws Exception {
		mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(401));
	}

	@Test
	void returnsNotFoundForMissingTicket() throws Exception {
		String token = authSupport.createUser(UserRole.USER).bearerToken();

		mockMvc.perform(get("/api/tickets/{id}", 99999999L)
				.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value(404))
				.andExpect(jsonPath("$.message").value("工单不存在: 99999999"));
	}

	private User saveUser(String prefix) {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		return userRepository.save(new User(
				prefix + "-" + suffix,
				prefix + "-" + suffix + "@example.com",
				"{bcrypt}password",
				UserRole.USER));
	}
}
