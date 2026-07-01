package com.project.demo.dto;

import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;

/**
 * 认证成功响应 DTO，返回 JWT 和当前用户概要信息。
 */
public class AuthResponse {

	private final String token;
	private final UserRole role;
	private final Long userId;
	private final String username;

	private AuthResponse(String token, User user) {
		this.token = token;
		this.role = user.getRole();
		this.userId = user.getId();
		this.username = user.getUsername();
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
}
