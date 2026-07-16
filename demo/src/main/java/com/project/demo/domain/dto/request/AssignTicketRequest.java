package com.project.demo.domain.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 管理员分配工单请求 DTO，指定目标客服用户 id。
 */
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
