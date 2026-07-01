package com.project.demo.user;

/**
 * 用户角色枚举。
 *
 * <p>角色同时用于 Spring Security 接口授权和服务层数据所有权判断。</p>
 */
public enum UserRole {
	USER,
	AGENT,
	ADMIN
}
