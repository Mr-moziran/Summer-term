package com.project.demo.domain.dto.response;

import com.project.demo.domain.model.Notification;
import com.project.demo.domain.enums.NotificationType;

import java.time.OffsetDateTime;

/**
 * 通知响应 DTO，供通知列表、WebSocket 推送和未读状态展示使用。
 */
public class NotificationResponse {

	private final Long id;
	private final NotificationType type;
	private final Long ticketId;
	private final String message;
	private final boolean read;
	private final OffsetDateTime createdAt;

	private NotificationResponse(Notification notification) {
		this.id = notification.getId();
		this.type = notification.getType();
		this.ticketId = notification.getTicket().getId();
		this.message = notification.getMessage();
		this.read = notification.isRead();
		this.createdAt = notification.getCreatedAt();
	}

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(notification);
	}

	public Long getId() {
		return id;
	}

	public NotificationType getType() {
		return type;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return read;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
