package com.project.demo.notification;

/**
 * 通知类型枚举。
 *
 * <p>用于区分通知来源场景，前端可据此选择图标、文案样式或跳转位置。</p>
 */
public enum NotificationType {
	STATUS_CHANGE,
	NEW_REPLY,
	ASSIGNED
}
