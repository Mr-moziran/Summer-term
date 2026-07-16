package com.project.demo.domain.dto.response;

/**
 * 未读通知数量响应 DTO，用于前端通知角标。
 */
public class UnreadNotificationCountResponse {

	private final long unreadCount;

	public UnreadNotificationCountResponse(long unreadCount) {
		this.unreadCount = unreadCount;
	}

	public long getUnreadCount() {
		return unreadCount;
	}
}
