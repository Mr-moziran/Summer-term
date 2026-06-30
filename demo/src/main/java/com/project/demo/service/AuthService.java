package com.project.demo.service;

import com.project.demo.dto.AuthResponse;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.exception.AuthenticationFailedException;
import com.project.demo.exception.DuplicateResourceException;
import com.project.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;

	private final JwtService jwtService;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

	public AuthService(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(String username, String email, String password) {
		String normalizedUsername = username.trim();
		String normalizedEmail = normalizeEmail(email);
		if (userRepository.existsByUsername(normalizedUsername)) {
			throw new DuplicateResourceException("用户名已存在");
		}
		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new DuplicateResourceException("邮箱已存在");
		}

		User user = userRepository.save(new User(
				normalizedUsername,
				normalizedEmail,
				passwordEncoder.encode(password),
				UserRole.USER));
		return AuthResponse.from(jwtService.generateToken(user), user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(String email, String password) {
		User user = userRepository.findByEmail(normalizeEmail(email))
				.orElseThrow(() -> new AuthenticationFailedException("邮箱或密码错误"));
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new AuthenticationFailedException("邮箱或密码错误");
		}
		return AuthResponse.from(jwtService.generateToken(user), user);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
