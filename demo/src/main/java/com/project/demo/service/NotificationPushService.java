package com.project.demo.service;

import com.project.demo.dto.NotificationResponse;
import com.project.demo.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPushService {

	static final String NOTIFICATION_DESTINATION = "/queue/notifications";

	private final SimpMessagingTemplate messagingTemplate;

	public NotificationPushService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void push(Notification notification) {
		messagingTemplate.convertAndSendToUser(
				notification.getUser().getId().toString(),
				NOTIFICATION_DESTINATION,
				NotificationResponse.from(notification));
	}
}
