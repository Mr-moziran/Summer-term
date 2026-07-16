package com.project.demo.service.notification;

import com.project.demo.domain.model.Notification;
import com.project.demo.domain.dto.response.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 通知推送服务。
 *
 * <p>把已持久化通知转换为前端 DTO，并发送到目标用户的 /queue/notifications。</p>
 */
@Service
public class NotificationPushService {

	private static final Logger log = LoggerFactory.getLogger(NotificationPushService.class);

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
		log.info("通知已推送: userId={}, type={}, ticketId={}",
				notification.getUser().getId(),
				notification.getType(),
				notification.getTicket().getId());
	}
}
