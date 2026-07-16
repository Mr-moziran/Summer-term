package com.project.demo.controller;

import com.project.demo.domain.dto.response.AdminDashboardResponse;
import com.project.demo.service.admin.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员后台首页接口控制器。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	public AdminDashboardController(AdminDashboardService adminDashboardService) {
		this.adminDashboardService = adminDashboardService;
	}

	@GetMapping
	public AdminDashboardResponse getDashboard() {
		return adminDashboardService.getDashboard();
	}
}
