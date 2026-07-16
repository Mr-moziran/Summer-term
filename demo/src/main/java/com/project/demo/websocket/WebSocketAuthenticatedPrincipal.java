package com.project.demo.websocket;

import java.security.Principal;

/**
 * WebSocket 会话中的用户身份。
 *
 * <p>STOMP 的用户目的地依赖 Principal 名称路由到 /user/queue/notifications，
 * 这里使用用户 id 字符串作为稳定名称。</p>
 */
public record WebSocketAuthenticatedPrincipal(String name) implements Principal {

	@Override
	public String getName() {
		return name;
	}
}
