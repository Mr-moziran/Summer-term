package com.project.demo.entity;

/**
 * 工单分类枚举。
 *
 * <p>分类由 AI 或人工确定，用于列表筛选、统计占比和相似案例 Prompt 上下文。</p>
 */
public enum TicketCategory {
	TECHNICAL,
	BILLING,
	COMPLAINT,
	OTHER
}
