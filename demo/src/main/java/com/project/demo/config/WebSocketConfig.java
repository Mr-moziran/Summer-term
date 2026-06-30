package com.project.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final WebSocketAuthenticationInterceptor authenticationInterceptor;

	private final WebSocketPrincipalHandshakeHandler handshakeHandler;

	public WebSocketConfig(
			WebSocketAuthenticationInterceptor authenticationInterceptor,
			WebSocketPrincipalHandshakeHandler handshakeHandler) {
		this.authenticationInterceptor = authenticationInterceptor;
		this.handshakeHandler = handshakeHandler;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/notifications")
				.setHandshakeHandler(handshakeHandler)
				.addInterceptors(authenticationInterceptor)
				.setAllowedOriginPatterns("*");
	}
}
