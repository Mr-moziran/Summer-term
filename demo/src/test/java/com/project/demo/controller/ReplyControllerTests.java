package com.project.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.repository.ReplyRepository;
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
class ReplyControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private ReplyRepository replyRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void submitterCanListReplies() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("我的工单", "需要帮助", submitter.user()));

		mockMvc.perform(get("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void agentCanListReplies() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		AuthUser agent = authSupport.createUser(UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("工单标题", "描述内容", submitter.user()));
		ticket.assignTo(agent.user());
		ticketRepository.save(ticket);

		mockMvc.perform(get("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, agent.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void userCannotListRepliesOfOtherTicket() throws Exception {
		AuthUser owner = authSupport.createUser(UserRole.USER);
		AuthUser other = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("他人工单", "别人的问题", owner.user()));

		mockMvc.perform(get("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, other.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void agentAddsReplyToAssignedTicket() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		AuthUser agent = authSupport.createUser(UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("需要回复", "请帮我", submitter.user()));
		ticket.assignTo(agent.user());
		ticketRepository.save(ticket);

		mockMvc.perform(post("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, agent.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "authorId": %d,
						  "content": "已处理，请确认",
						  "aiAdopted": false
						}
						""".formatted(agent.user().getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.ticketId").value(ticket.getId()))
				.andExpect(jsonPath("$.authorId").value(agent.user().getId()))
				.andExpect(jsonPath("$.content").value("已处理，请确认"))
				.andExpect(jsonPath("$.aiDraft").value(false))
				.andExpect(jsonPath("$.aiAdopted").value(false));
	}

	@Test
	void adminAddsReplyToAnyTicket() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		AuthUser admin = authSupport.createUser(UserRole.ADMIN);
		Ticket ticket = ticketRepository.save(new Ticket("管理员回复", "工单内容", submitter.user()));

		mockMvc.perform(post("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "authorId": %d,
						  "content": "管理员介入处理"
						}
						""".formatted(admin.user().getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.authorId").value(admin.user().getId()))
				.andExpect(jsonPath("$.content").value("管理员介入处理"));
	}

	@Test
	void normalUserCannotAddReply() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("我的工单", "求助", submitter.user()));

		mockMvc.perform(post("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, submitter.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "authorId": %d,
						  "content": "我自己回复一下"
						}
						""".formatted(submitter.user().getId())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void agentAddsAiAdoptedReply() throws Exception {
		AuthUser submitter = authSupport.createUser(UserRole.USER);
		AuthUser agent = authSupport.createUser(UserRole.AGENT);
		Ticket ticket = ticketRepository.save(new Ticket("AI辅助", "采纳AI建议", submitter.user()));
		ticket.assignTo(agent.user());
		ticketRepository.save(ticket);

		mockMvc.perform(post("/api/tickets/{ticketId}/replies", ticket.getId())
				.header(HttpHeaders.AUTHORIZATION, agent.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "authorId": %d,
						  "content": "根据AI建议，建议您重启设备后再试",
						  "aiAdopted": true
						}
						""".formatted(agent.user().getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.aiAdopted").value(true));
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
