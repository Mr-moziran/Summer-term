package com.project.demo.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {

	@NotNull(message = "客服ID不能为空")
	private Long assigneeId;

	public Long getAssigneeId() {
		return assigneeId;
	}

	public void setAssigneeId(Long assigneeId) {
		this.assigneeId = assigneeId;
	}
}
