package com.project.demo.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO，包含用户名、邮箱和密码。
 */
public class RegisterRequest {

	@NotBlank(message = "用户名不能为空")
	@Size(max = 50, message = "用户名不能超过50个字符")
	private String username;

	@NotBlank(message = "邮箱不能为空")
	@Email(message = "邮箱格式不正确")
	@Size(max = 100, message = "邮箱不能超过100个字符")
	private String email;

	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 72, message = "密码长度必须在6到72个字符之间")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
