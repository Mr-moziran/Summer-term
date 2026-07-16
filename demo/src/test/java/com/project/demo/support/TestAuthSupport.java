package com.project.demo.support;

import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.repository.UserRepository;
import com.project.demo.security.JwtService;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TestAuthSupport {

	private final UserRepository userRepository;

	private final JwtService jwtService;

	public TestAuthSupport(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	public AuthUser createUser(UserRole role) {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		User user = userRepository.save(new User(
				"test-" + role.name().toLowerCase() + "-" + suffix,
				"test-" + role.name().toLowerCase() + "-" + suffix + "@example.com",
				"{bcrypt}password",
				role));
		return new AuthUser(user, jwtService.generateToken(user));
	}

	public record AuthUser(User user, String token) {
		public String bearerToken() {
			return "Bearer " + token;
		}
	}
}
