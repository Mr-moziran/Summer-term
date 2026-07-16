package com.project.demo.domain.dto.response;

/**
 * 管理员后台模块入口响应。
 */
public class AdminModuleResponse {

	private final String key;

	private final String name;

	private final String path;

	private final String description;

	public AdminModuleResponse(String key, String name, String path, String description) {
		this.key = key;
		this.name = name;
		this.path = path;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public String getDescription() {
		return description;
	}
}
