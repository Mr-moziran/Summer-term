package com.project.demo.websocket;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket Principal 绑定器。
 *
 * <p>从握手属性中取出认证通过的用户 id，构造成 Spring WebSocket 用户身份。</p>
 */
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
