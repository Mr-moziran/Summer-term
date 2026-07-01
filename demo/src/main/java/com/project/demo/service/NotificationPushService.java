package com.project.demo.service;

import com.project.demo.dto.NotificationResponse;
import com.project.demo.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 通知推送服务。
 *
 * <p>把已持久化通知转换为前端 DTO，并发送到目标用户的 /queue/notifications。</p>
 */
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
