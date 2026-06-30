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
import com.project.demo.support.TestAuthSupport.AuthUser;
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
		AuthUser submitter = authSupport.createUser(UserRole.USER);

		mockMvc.perform(post("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "submitterId": %d,
						  "title": "无法登录系统",
						  "description": "昨天开始一直提示密码错误"
						}
						""".formatted(submitter.user().getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("无法登录系统"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.submitterId").value(submitter.user().getId()))
				.andExpect(jsonPath("$.aiClassified").value(false));
	}

	@Test
	void rejectsTicketCreationForOtherSubmitter() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		User otherSubmitter = saveUser("create-other");

		mockMvc.perform(post("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "submitterId": %d,
						  "title": "越权创建工单",
						  "description": "不能替其他用户提交"
						}
						""".formatted(otherSubmitter.getId())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void listsTicketsWithSubmitterFilter() throws Exception {
		AuthUser alice = authSupport.createUser(UserRole.USER);
		User bob = saveUser("list-bob");
		ticketRepository.save(new Ticket("Alice ticket", "Alice description", alice.user()));
		ticketRepository.save(new Ticket("Bob ticket", "Bob description", bob));

		mockMvc.perform(get("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, alice.bearerToken())
				.param("submitterId", alice.user().getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("Alice ticket"))
				.andExpect(jsonPath("$.content[0].submitterId").value(alice.user().getId()));
	}

	@Test
	void rejectsTicketListForOtherSubmitterFilter() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		User otherSubmitter = saveUser("list-other");

		mockMvc.perform(get("/api/tickets")
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken())
				.param("submitterId", otherSubmitter.getId().toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
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

	@Test
	void rejectsTicketDetailForOtherSubmitter() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		User otherSubmitter = saveUser("detail-other");
		Ticket ticket = ticketRepository.save(new Ticket("Other ticket", "Other description", otherSubmitter));

		mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void ratesOwnTicket() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("Rate ticket", "Please rate", submitter.user()));

		mockMvc.perform(post("/api/tickets/{id}/rate", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "rating": 5,
						  "comment": "处理很及时"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticket.getId()))
				.andExpect(jsonPath("$.rating").value(5))
				.andExpect(jsonPath("$.ratingComment").value("处理很及时"));
	}

	@Test
	void rejectsRatingForOtherSubmitter() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		User otherSubmitter = saveUser("rate-other");
		Ticket ticket = ticketRepository.save(new Ticket("Other ticket", "Other description", otherSubmitter));

		mockMvc.perform(post("/api/tickets/{id}/rate", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "rating": 5,
						  "comment": "不能评价别人的工单"
						}
						"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void rejectsRatingOutsideRange() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("Rate ticket", "Please rate", submitter.user()));

		mockMvc.perform(post("/api/tickets/{id}/rate", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "rating": 6,
						  "comment": "超出范围"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(400));
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
