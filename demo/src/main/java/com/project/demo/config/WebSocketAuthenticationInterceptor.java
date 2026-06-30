package com.project.demo.config;

import com.project.demo.entity.User;
import com.project.demo.entity.UserStatus;
import com.project.demo.repository.UserRepository;
import com.project.demo.service.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

	static final String PRINCIPAL_ATTRIBUTE = "principal";

	private final JwtService jwtService;

	private final UserRepository userRepository;

	public WebSocketAuthenticationInterceptor(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
		if (token == null || token.isBlank()) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}
		try {
			JwtService.JwtClaims claims = jwtService.verify(token);
			User user = userRepository.findById(claims.userId())
					.orElseThrow(() -> new IllegalArgumentException("用户不存在"));
			if (user.getStatus() != UserStatus.ACTIVE) {
				throw new IllegalArgumentException("用户已禁用");
			}
			attributes.put(PRINCIPAL_ATTRIBUTE, new WebSocketAuthenticatedPrincipal(user.getId().toString()));
			return true;
		}
		catch (IllegalArgumentException exception) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception) {
	}
}
