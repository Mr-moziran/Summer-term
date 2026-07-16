package com.project.demo.domain.dto.response;

import java.util.List;

/**
 * 管理员后台首页聚合响应。
 */
public class AdminDashboardResponse {

	private final AdminStatsResponse stats;

	private final List<AdminModuleResponse> modules;

	public AdminDashboardResponse(AdminStatsResponse stats, List<AdminModuleResponse> modules) {
		this.stats = stats;
		this.modules = List.copyOf(modules);
	}

	public AdminStatsResponse getStats() {
		return stats;
	}

	public List<AdminModuleResponse> getModules() {
		return modules;
	}
}
