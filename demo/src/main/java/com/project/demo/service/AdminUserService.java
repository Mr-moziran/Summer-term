package com.project.demo.service;

import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.entity.UserStatus;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

	private final UserRepository userRepository;

	public AdminUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public Page<User> listUsers(UserRole role, UserStatus status, Pageable pageable) {
		return userRepository.findAll(buildSpecification(role, status), pageable);
	}

	@Transactional
	public User updateStatus(Long userId, UserStatus status) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
		user.changeStatus(status);
		return user;
	}

	private Specification<User> buildSpecification(UserRole role, UserStatus status) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (role != null) {
				predicates.add(criteriaBuilder.equal(root.get("role"), role));
			}
			if (status != null) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status));
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}
}
