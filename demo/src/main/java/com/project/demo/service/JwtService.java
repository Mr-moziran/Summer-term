package com.project.demo.service;

import com.project.demo.entity.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	private String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
