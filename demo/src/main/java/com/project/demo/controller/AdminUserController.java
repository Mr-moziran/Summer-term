package com.project.demo.controller;

import com.project.demo.dto.PageResponse;
import com.project.demo.dto.UpdateUserStatusRequest;
import com.project.demo.dto.UserResponse;
import com.project.demo.entity.UserRole;
import com.project.demo.entity.UserStatus;
import com.project.demo.service.AdminUserService;
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
