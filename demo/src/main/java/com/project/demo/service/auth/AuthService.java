package com.project.demo.service.auth;

import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.domain.enums.UserStatus;
import com.project.demo.domain.dto.response.AuthResponse;
import com.project.demo.exception.AuthenticationFailedException;
import com.project.demo.exception.DuplicateResourceException;
import com.project.demo.repository.UserRepository;
import com.project.demo.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务。
 *
 * <p>负责用户注册、密码哈希、登录校验和 JWT 颁发。邮箱在保存和登录前统一规整，避免大小写造成重复账号。</p>
 */
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
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new AuthenticationFailedException("账号已禁用");
		}
		return AuthResponse.from(jwtService.generateToken(user), user);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
