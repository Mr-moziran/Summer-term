package com.project.demo.controller;

import com.project.demo.domain.dto.response.AdminStatsResponse;
import com.project.demo.domain.dto.response.AgentPerformanceResponse;
import com.project.demo.service.admin.AdminStatsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员统计接口控制器。
 *
 * <p>提供数据大盘和客服绩效统计，面向管理员端图表与报表页面。</p>
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

	private final AdminStatsService adminStatsService;

	public AdminStatsController(AdminStatsService adminStatsService) {
		this.adminStatsService = adminStatsService;
	}

	@GetMapping
	public AdminStatsResponse getStats() {
		return adminStatsService.getStats();
	}

	@GetMapping("/agents")
	public List<AgentPerformanceResponse> listAgentPerformance() {
		return adminStatsService.listAgentPerformance();
	}
}
