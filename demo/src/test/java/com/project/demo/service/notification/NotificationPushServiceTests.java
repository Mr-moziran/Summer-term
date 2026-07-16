package com.project.demo.service.notification;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.project.demo.domain.model.Notification;
import com.project.demo.domain.enums.NotificationType;
import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.domain.dto.response.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class NotificationPushServiceTests {

	@Test
	void pushesNotificationToUserQueue() {
		SimpMessagingTemplate messagingTemplate = org.mockito.Mockito.mock(SimpMessagingTemplate.class);
		NotificationPushService service = new NotificationPushService(messagingTemplate);
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		setId(submitter, 1L);
		Ticket ticket = new Ticket("无法登录", "密码错误", submitter);
		setId(ticket, 10L);
		Notification notification = new Notification(
				submitter,
				NotificationType.NEW_REPLY,
				ticket,
				"您的工单「无法登录」有新的回复");
		setId(notification, 100L);

		service.push(notification);

		ArgumentCaptor<NotificationResponse> payloadCaptor = ArgumentCaptor.forClass(NotificationResponse.class);
		verify(messagingTemplate).convertAndSendToUser(
				eq("1"),
				eq(NotificationPushService.NOTIFICATION_DESTINATION),
				payloadCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(payloadCaptor.getValue().getId()).isEqualTo(100L);
		org.assertj.core.api.Assertions.assertThat(payloadCaptor.getValue().getTicketId()).isEqualTo(10L);
	}

	private void setId(Object entity, Long id) {
		try {
			java.lang.reflect.Field field = entity.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(entity, id);
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
