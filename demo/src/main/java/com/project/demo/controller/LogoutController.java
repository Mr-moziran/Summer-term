package com.project.demo.controller;

import com.project.demo.security.JwtService;
import com.project.demo.security.TokenBlacklist;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证退出接口。
 *
 * <p>JWT 本身无状态，退出通过将 token 加入黑名单实现，过滤器会拒绝黑名单中的 token。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class LogoutController {

	private final TokenBlacklist tokenBlacklist;

	private final JwtService jwtService;

	public LogoutController(TokenBlacklist tokenBlacklist, JwtService jwtService) {
		this.tokenBlacklist = tokenBlacklist;
		this.jwtService = jwtService;
	}

	@PostMapping("/logout")
	public void logout(@RequestHeader("Authorization") String authorization) {
		if (authorization != null && authorization.startsWith("Bearer ")) {
			String token = authorization.substring(7);
			long expiresAt = jwtService.extractExpiresAt(token);
			tokenBlacklist.add(token, expiresAt);
		}
	}
}
