package com.project.demo.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌黑名单。
 *
 * <p>用户登出时将 token 加入黑名单，JwtAuthenticationFilter 认证时检查。
 * 黑名单条目在超过 token 过期时间后自动清理，防止内存泄漏。</p>
 */
@Component
public class TokenBlacklist {

	private final Map<String, Long> invalidated = new ConcurrentHashMap<>();

	/**
	 * 将 token 加入黑名单，记录失效时间戳用于清理。
	 */
	public void add(String token, long expiresAtEpochSecond) {
		invalidated.put(token, expiresAtEpochSecond);
	}

	/**
	 * 检查 token 是否已被登出。
	 */
	public boolean contains(String token) {
		return invalidated.containsKey(token);
	}

	/**
	 * 清理已过期的黑名单条目。
	 */
	public void evictExpired() {
		long now = System.currentTimeMillis() / 1000;
		invalidated.entrySet().removeIf(entry -> entry.getValue() < now);
	}
}
