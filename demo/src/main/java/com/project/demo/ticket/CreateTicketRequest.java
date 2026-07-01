package com.project.demo.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建工单请求 DTO，包含提交人、标题和问题描述。
 */
public class CreateTicketRequest {

	@NotNull(message = "提交人ID不能为空")
	private Long submitterId;

	@NotBlank(message = "工单标题不能为空")
	@Size(max = 200, message = "工单标题不能超过200个字符")
	private String title;

	@NotBlank(message = "工单描述不能为空")
	private String description;

	public Long getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(Long submitterId) {
		this.submitterId = submitterId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
