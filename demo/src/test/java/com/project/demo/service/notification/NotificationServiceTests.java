package com.project.demo.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.demo.domain.model.Notification;
import com.project.demo.domain.enums.NotificationType;
import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.repository.NotificationRepository;
import org.junit.jupiter.api.Test;

class NotificationServiceTests {

	@Test
	void savesAndPushesNotification() {
		NotificationRepository notificationRepository = org.mockito.Mockito.mock(NotificationRepository.class);
		NotificationPushService notificationPushService = org.mockito.Mockito.mock(NotificationPushService.class);
		NotificationService service = new NotificationService(notificationRepository, notificationPushService);
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		Ticket ticket = new Ticket("无法登录", "密码错误", submitter);
		Notification savedNotification = new Notification(
				submitter,
				NotificationType.STATUS_CHANGE,
				ticket,
				"状态已更新");
		when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class)))
				.thenReturn(savedNotification);

		Notification notification = service.createNotification(
				submitter,
				NotificationType.STATUS_CHANGE,
				ticket,
				"状态已更新");

		assertThat(notification).isSameAs(savedNotification);
		verify(notificationPushService).push(savedNotification);
	}
}
