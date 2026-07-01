package com.project.demo.common.exception;

/**
 * 唯一资源冲突异常，对应 HTTP 409。
 */
public class DuplicateResourceException extends RuntimeException {

	public DuplicateResourceException(String message) {
		super(message);
	}
}
