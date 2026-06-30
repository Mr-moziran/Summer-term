package com.project.demo.controller;

import com.project.demo.dto.AdminStatsResponse;
import com.project.demo.dto.AgentPerformanceResponse;
import com.project.demo.service.AdminStatsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
