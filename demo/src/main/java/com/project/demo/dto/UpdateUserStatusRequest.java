package com.project.demo.dto;

import com.project.demo.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

	@NotNull(message = "用户状态不能为空")
	private UserStatus status;

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}
}
