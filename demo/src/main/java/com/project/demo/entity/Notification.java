package com.project.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 通知实体。
 *
 * <p>通知作为 WebSocket 实时推送的持久化补偿：即使用户离线，也可以通过 REST 通知列表读取历史消息。</p>
 */
@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationType type;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected Notification() {
	}

	public Notification(User user, NotificationType type, Ticket ticket, String message) {
		this.user = user;
		this.type = type;
		this.ticket = ticket;
		this.message = message;
	}

	public void markRead() {
		this.isRead = true;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public NotificationType getType() {
		return type;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return isRead;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
