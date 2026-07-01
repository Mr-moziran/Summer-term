package com.project.demo.ticket;

import jakarta.validation.constraints.NotNull;

/**
 * 工单状态更新请求 DTO，指定目标状态。
 */
public class UpdateTicketStatusRequest {

	@NotNull(message = "工单状态不能为空")
	private TicketStatus status;

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}
}
