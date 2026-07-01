package com.project.demo.auth;

/**
 * 登录认证失败异常，对应 HTTP 401。
 */
public class AuthenticationFailedException extends RuntimeException {

	public AuthenticationFailedException(String message) {
		super(message);
	}
}
