package com.project.demo.entity;

/**
 * 工单状态枚举。
 *
 * <p>状态驱动工单生命周期：提交后待分配，分配后处理，解决后可关闭。</p>
 */
public enum TicketStatus {
	PENDING,
	ASSIGNED,
	PROCESSING,
	RESOLVED,
	CLOSED
}
