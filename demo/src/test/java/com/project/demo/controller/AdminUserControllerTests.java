package com.project.demo.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
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
class AdminUserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void adminListsUsersWithoutPasswordField() throws Exception {
		AuthUser admin = authSupport.createUser(UserRole.ADMIN);
		User user = saveUser("admin-list-user", UserRole.USER);

		mockMvc.perform(get("/api/admin/users")
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].id", hasItem(user.getId().intValue())))
				.andExpect(jsonPath("$.content[*].username", hasItem(user.getUsername())))
				.andExpect(jsonPath("$.content[*].role", hasItem("USER")))
				.andExpect(jsonPath("$.content[*].status", hasItem("ACTIVE")))
				.andExpect(jsonPath("$.content[*].password").doesNotExist());
	}

	@Test
	void userCannotListUsers() throws Exception {
		AuthUser user = authSupport.createUser(UserRole.USER);

		mockMvc.perform(get("/api/admin/users")
				.header(HttpHeaders.AUTHORIZATION, user.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void adminUpdatesUserStatus() throws Exception {
		AuthUser admin = authSupport.createUser(UserRole.ADMIN);
		User user = saveUser("admin-disable-user", UserRole.USER);

		mockMvc.perform(patch("/api/admin/users/{id}/status", user.getId())
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "status": "DISABLED"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.status").value("DISABLED"));

		mockMvc.perform(get("/api/admin/users")
				.header(HttpHeaders.AUTHORIZATION, admin.bearerToken())
				.param("status", "DISABLED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].id", hasItem(user.getId().intValue())))
				.andExpect(jsonPath("$.content[*].id", not(hasItem(admin.user().getId().intValue()))));
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
