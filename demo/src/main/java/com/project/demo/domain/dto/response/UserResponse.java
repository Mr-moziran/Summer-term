package com.project.demo.domain.dto.response;

import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.domain.enums.UserStatus;

import java.time.OffsetDateTime;

/**
 * 用户响应 DTO，展示用户基本资料、角色、状态和时间字段。
 */
public class UserResponse {

	private final Long id;
	private final String username;
	private final String email;
	private final UserRole role;
	private final UserStatus status;
	private final String avatarUrl;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;

	private UserResponse(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.role = user.getRole();
		this.status = user.getStatus();
		this.avatarUrl = user.getAvatarUrl();
		this.createdAt = user.getCreatedAt();
		this.updatedAt = user.getUpdatedAt();
	}

	public static UserResponse from(User user) {
		return new UserResponse(user);
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public UserRole getRole() {
		return role;
	}

	public UserStatus getStatus() {
		return status;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
