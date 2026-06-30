package com.project.demo.dto;

import java.time.OffsetDateTime;

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
