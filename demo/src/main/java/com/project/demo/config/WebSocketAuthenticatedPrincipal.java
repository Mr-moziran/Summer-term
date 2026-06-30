package com.project.demo.config;

import java.security.Principal;

public record WebSocketAuthenticatedPrincipal(String name) implements Principal {

	@Override
	public String getName() {
		return name;
	}
}
