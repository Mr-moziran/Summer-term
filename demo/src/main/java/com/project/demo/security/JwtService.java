package com.project.demo.security;

import com.project.demo.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT 生成与校验服务。
 *
 * <p>Token 中保存用户 id、角色和过期时间；业务层不直接解析 JWT，而通过认证过滤器写入安全上下文。</p>
 */
@Service
public class JwtService {

	private final String secret;

	private final long expirationMinutes;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
		this.secret = secret;
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expirationMinutes * 60);
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = "{"
				+ "\"sub\":\"" + escapeJson(user.getEmail()) + "\","
				+ "\"userId\":" + user.getId() + ","
				+ "\"username\":\"" + escapeJson(user.getUsername()) + "\","
				+ "\"role\":\"" + user.getRole().name() + "\","
				+ "\"iat\":" + now.getEpochSecond() + ","
				+ "\"exp\":" + expiresAt.getEpochSecond()
				+ "}";

		String encodedHeader = base64Url(header.getBytes(StandardCharsets.UTF_8));
		String encodedPayload = base64Url(payload.getBytes(StandardCharsets.UTF_8));
		String signature = sign(encodedHeader + "." + encodedPayload);
		return encodedHeader + "." + encodedPayload + "." + signature;
	}

	public JwtClaims verify(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("JWT格式不正确");
		}
		String signingInput = parts[0] + "." + parts[1];
		String expectedSignature = sign(signingInput);
		if (!constantTimeEquals(expectedSignature, parts[2])) {
			throw new IllegalArgumentException("JWT签名不正确");
		}
		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		long exp = Long.parseLong(extractNumber(payload, "exp"));
		if (Instant.now().getEpochSecond() >= exp) {
			throw new IllegalArgumentException("JWT已过期");
		}
		return new JwtClaims(
				Long.parseLong(extractNumber(payload, "userId")),
				extractString(payload, "sub"),
				extractString(payload, "username"),
				extractString(payload, "role"));
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException("无法生成JWT签名", exception);
		}
	}

	private boolean constantTimeEquals(String left, String right) {
		byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
		byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
		if (leftBytes.length != rightBytes.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < leftBytes.length; i++) {
			result |= leftBytes[i] ^ rightBytes[i];
		}
		return result == 0;
	}

	private String extractString(String json, String field) {
		String marker = "\"" + field + "\":\"";
		int start = json.indexOf(marker);
		if (start < 0) {
			throw new IllegalArgumentException("JWT缺少字段: " + field);
		}
		start += marker.length();
		int end = json.indexOf('"', start);
		if (end < 0) {
			throw new IllegalArgumentException("JWT字段格式不正确: " + field);
		}
		return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
	}

	private String extractNumber(String json, String field) {
		String marker = "\"" + field + "\":";
		int start = json.indexOf(marker);
		if (start < 0) {
			throw new IllegalArgumentException("JWT缺少字段: " + field);
		}
		start += marker.length();
		int end = start;
		while (end < json.length() && Character.isDigit(json.charAt(end))) {
			end++;
		}
		return json.substring(start, end);
	}

	private String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public record JwtClaims(Long userId, String email, String username, String role) {
	}
}
