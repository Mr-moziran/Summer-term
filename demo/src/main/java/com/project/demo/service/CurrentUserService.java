package com.project.demo.service;

import com.project.demo.entity.User;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
