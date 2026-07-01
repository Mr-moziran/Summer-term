package com.project.demo.common.dto;

import java.time.OffsetDateTime;

/**
 * 统一错误响应 DTO，所有 REST 错误都按 code/message/data/timestamp 输出。
 */
public class ApiErrorResponse {

	private final int code;

	private final String message;

	private final Object data;

	private final OffsetDateTime timestamp;

	public ApiErrorResponse(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
		this.timestamp = OffsetDateTime.now();
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Object getData() {
		return data;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}
}
