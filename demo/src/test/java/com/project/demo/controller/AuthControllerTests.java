package com.project.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/db/schema.sql")
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Test
	void registersUserWithBcryptPasswordAndToken() throws Exception {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String email = "auth-register-" + suffix + "@example.com";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "auth-user-%s",
						  "email": "%s",
						  "password": "secret123"
						}
						""".formatted(suffix, email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token", containsString(".")))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.userId").isNumber())
				.andExpect(jsonPath("$.username").value("auth-user-" + suffix));

		var user = userRepository.findByEmail(email).orElseThrow();
		assertThat(user.getPassword()).isNotEqualTo("secret123");
		assertThat(new BCryptPasswordEncoder().matches("secret123", user.getPassword())).isTrue();
	}

	@Test
	void logsInExistingUser() throws Exception {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String email = "auth-login-" + suffix + "@example.com";
		register("auth-login-" + suffix, email, "secret123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "%s",
						  "password": "secret123"
						}
						""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", containsString(".")))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void rejectsInvalidPassword() throws Exception {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String email = "auth-invalid-" + suffix + "@example.com";
		register("auth-invalid-" + suffix, email, "secret123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "%s",
						  "password": "wrong-password"
						}
						""".formatted(email)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(401))
				.andExpect(jsonPath("$.message").value("邮箱或密码错误"));
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String email = "auth-duplicate-" + suffix + "@example.com";
		register("auth-duplicate-" + suffix, email, "secret123");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "auth-other-%s",
						  "email": "%s",
						  "password": "secret123"
						}
						""".formatted(suffix, email)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value(409))
				.andExpect(jsonPath("$.message").value("邮箱已存在"));
	}

	private void register(String username, String email, String password) throws Exception {
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "%s",
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(username, email, password)))
				.andExpect(status().isCreated());
	}
}
