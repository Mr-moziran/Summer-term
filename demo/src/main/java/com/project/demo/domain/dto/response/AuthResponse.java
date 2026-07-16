package com.project.demo.domain.dto.response;

import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;

/**
 * 认证成功响应 DTO，返回 JWT 和当前用户概要信息。
 */
public class AuthResponse {

	private final String token;
	private final UserRole role;
	private final Long userId;
	private final String username;
	private final String homePath;

	private AuthResponse(String token, User user) {
		this.token = token;
		this.role = user.getRole();
		this.userId = user.getId();
		this.username = user.getUsername();
		this.homePath = homePath(user.getRole());
	}

	public static AuthResponse from(String token, User user) {
		return new AuthResponse(token, user);
	}

	public String getToken() {
		return token;
	}

	public UserRole getRole() {
		return role;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getHomePath() {
		return homePath;
	}

	private String homePath(UserRole role) {
		return switch (role) {
			case ADMIN -> "/admin/dashboard";
			case AGENT -> "/agent/tickets";
			case USER -> "/my-tickets";
		};
	}
}
