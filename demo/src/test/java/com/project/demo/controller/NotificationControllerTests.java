package com.project.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.demo.entity.Notification;
import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.UserRole;
import com.project.demo.repository.NotificationRepository;
import com.project.demo.repository.TicketRepository;
import com.project.demo.support.TestAuthSupport;
import com.project.demo.support.TestAuthSupport.AuthUser;
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
class NotificationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private TestAuthSupport authSupport;

	@Test
	void listsOnlyCurrentUserNotifications() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		AuthUser otherUser = authSupport.createUser(UserRole.USER);
		Ticket currentTicket = ticketRepository.save(new Ticket("Current ticket", "Current description", currentUser.user()));
		Ticket otherTicket = ticketRepository.save(new Ticket("Other ticket", "Other description", otherUser.user()));
		notificationRepository.save(new Notification(
				currentUser.user(),
				NotificationType.STATUS_CHANGE,
				currentTicket,
				"您的工单状态已更新"));
		notificationRepository.save(new Notification(
				otherUser.user(),
				NotificationType.STATUS_CHANGE,
				otherTicket,
				"其他用户通知"));

		mockMvc.perform(get("/api/notifications")
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].ticketId").value(currentTicket.getId()))
				.andExpect(jsonPath("$.content[0].type").value("STATUS_CHANGE"))
				.andExpect(jsonPath("$.content[0].message").value("您的工单状态已更新"))
				.andExpect(jsonPath("$.content[0].read").value(false));
	}

	@Test
	void marksOwnNotificationAsRead() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("Current ticket", "Current description", currentUser.user()));
		Notification notification = notificationRepository.save(new Notification(
				currentUser.user(),
				NotificationType.NEW_REPLY,
				ticket,
				"您的工单有新回复"));

		mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(notification.getId()))
				.andExpect(jsonPath("$.read").value(true));

		mockMvc.perform(get("/api/notifications")
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken())
				.param("unreadOnly", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(0)));
	}

	@Test
	void rejectsMarkingOtherUserNotificationAsRead() throws Exception {
		AuthUser currentUser = authSupport.createUser(UserRole.USER);
		AuthUser otherUser = authSupport.createUser(UserRole.USER);
		Ticket ticket = ticketRepository.save(new Ticket("Other ticket", "Other description", otherUser.user()));
		Notification notification = notificationRepository.save(new Notification(
				otherUser.user(),
				NotificationType.NEW_REPLY,
				ticket,
				"其他用户通知"));

		mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
				.header(HttpHeaders.AUTHORIZATION, currentUser.bearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(403));
	}
}
