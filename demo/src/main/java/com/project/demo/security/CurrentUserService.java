package com.project.demo.security;

import com.project.demo.user.User;
import com.project.demo.common.exception.ResourceNotFoundException;
import com.project.demo.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户解析服务。
 *
 * <p>从 Spring Security 上下文读取 JWT 过滤器写入的用户 id，再查询数据库获得最新用户状态。</p>
 */
@Service
public class CurrentUserService {

	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new ResourceNotFoundException("当前用户不存在");
		}
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
	}
}
