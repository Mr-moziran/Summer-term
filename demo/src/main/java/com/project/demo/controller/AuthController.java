package com.project.demo.controller;

import com.project.demo.domain.dto.request.LoginRequest;
import com.project.demo.domain.dto.request.RegisterRequest;
import com.project.demo.domain.dto.response.AuthResponse;
import com.project.demo.security.JwtService;
import com.project.demo.security.TokenBlacklist;
import com.project.demo.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口控制器。
 *
 * <p>提供注册、登录和退出接口。JWT 无状态，退出通过将 token 加入黑名单实现。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	private final TokenBlacklist tokenBlacklist;

	private final JwtService jwtService;

	public AuthController(AuthService authService, TokenBlacklist tokenBlacklist, JwtService jwtService) {
		this.authService = authService;
		this.tokenBlacklist = tokenBlacklist;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.getEmail(), request.getPassword());
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
