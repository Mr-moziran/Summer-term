package com.project.demo.admin;

import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import com.project.demo.user.UserStatus;
import com.project.demo.common.exception.ResourceNotFoundException;
import com.project.demo.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员用户服务。
 *
 * <p>提供用户列表过滤和账号状态变更能力，用于管理员端用户管理页面。</p>
 */
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
