package com.project.demo.controller;

import com.project.demo.domain.dto.request.UpdateUserStatusRequest;
import com.project.demo.domain.dto.response.PageResponse;
import com.project.demo.domain.dto.response.UserResponse;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.domain.enums.UserStatus;
import com.project.demo.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户管理接口控制器。
 *
 * <p>提供用户列表筛选和账号启用/禁用操作，入口权限由 SecurityConfig 限制为 ADMIN。</p>
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	public PageResponse<UserResponse> listUsers(
			@RequestParam(required = false) UserRole role,
			@RequestParam(required = false) UserStatus status,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponse.from(adminUserService.listUsers(role, status, pageable).map(UserResponse::from));
	}

	@PatchMapping("/{id}/status")
	public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
		return UserResponse.from(adminUserService.updateStatus(id, request.getStatus()));
	}
}
