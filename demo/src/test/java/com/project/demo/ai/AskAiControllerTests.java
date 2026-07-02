package com.project.demo.ai;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.support.TestAuthSupport;
import com.project.demo.support.TestAuthSupport.AuthUser;
import com.project.demo.ticket.TicketRepository;
import com.project.demo.ticket.TicketStatus;
import com.project.demo.user.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/db/schema.sql")
class AskAiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestAuthSupport authSupport;

	@Autowired
	private TicketRepository ticketRepository;

	@Test
	void unauthenticatedUserCannotAskAi() throws Exception {
		mockMvc.perform(post("/api/ai/ask")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "question": "忘记密码怎么办"
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(401));
	}

	@Test
	void loggedInUserGetsKnowledgeBasedAnswer() throws Exception {
		AuthUser user = authSupport.createUser(UserRole.USER);

		mockMvc.perform(post("/api/ai/ask")
				.header(HttpHeaders.AUTHORIZATION, user.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "question": "忘记密码怎么办"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("ANSWERED"))
				.andExpect(jsonPath("$.answer", containsString("账号帮助")))
				.andExpect(jsonPath("$.references[0].title").value("账号帮助"))
				.andExpect(jsonPath("$.ticket").doesNotExist());
	}

	@Test
	void explicitHumanTransferCreatesPendingTicketForCurrentUser() throws Exception {
		AuthUser user = authSupport.createUser(UserRole.USER);

		mockMvc.perform(post("/api/ai/ask")
				.header(HttpHeaders.AUTHORIZATION, user.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "question": "我忘记密码了，转人工处理"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("ESCALATED"))
				.andExpect(jsonPath("$.ticket.id").isNumber())
				.andExpect(jsonPath("$.ticket.status").value("PENDING"))
				.andExpect(jsonPath("$.ticket.title", containsString("AI未能解答")));

		org.assertj.core.api.Assertions.assertThat(ticketRepository.findAll())
				.anySatisfy(ticket -> {
					org.assertj.core.api.Assertions.assertThat(ticket.getSubmitter().getId()).isEqualTo(user.user().getId());
					org.assertj.core.api.Assertions.assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PENDING);
					org.assertj.core.api.Assertions.assertThat(ticket.getTitle()).contains("AI未能解答");
				});
	}

	@TestConfiguration
	static class AskAiControllerTestConfiguration {

		@Bean
		@Primary
		KnowledgeSearch testKnowledgeSearch() {
			return (question, topK, similarityThreshold) -> List.of(
					new KnowledgeDocument(1L, "账号帮助", "请在登录页点击忘记密码。", 0.91));
		}
	}
}
