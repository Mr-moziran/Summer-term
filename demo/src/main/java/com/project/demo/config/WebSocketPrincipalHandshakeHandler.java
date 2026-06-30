package com.project.demo.config;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class WebSocketPrincipalHandshakeHandler extends DefaultHandshakeHandler {

	@Override
	protected Principal determineUser(
			ServerHttpRequest request,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		Object principal = attributes.get(WebSocketAuthenticationInterceptor.PRINCIPAL_ATTRIBUTE);
		if (principal instanceof Principal authenticatedPrincipal) {
			return authenticatedPrincipal;
		}
		return super.determineUser(request, wsHandler, attributes);
	}
}
