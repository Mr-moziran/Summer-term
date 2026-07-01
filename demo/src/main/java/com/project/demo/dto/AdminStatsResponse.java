package com.project.demo.dto;

import java.util.Map;

/**
 * 管理员数据大盘响应 DTO，包含今日工单、待处理数量、平均响应、分类分布和 AI 采纳率。
 */
public class AdminStatsResponse {

	private final long todayTotal;
	private final long pendingCount;
	private final double avgResponseMinutes;
	private final Map<String, Double> categoryDistribution;
	private final double aiAdoptionRate;

	public AdminStatsResponse(
			long todayTotal,
			long pendingCount,
			double avgResponseMinutes,
			Map<String, Double> categoryDistribution,
			double aiAdoptionRate) {
		this.todayTotal = todayTotal;
		this.pendingCount = pendingCount;
		this.avgResponseMinutes = avgResponseMinutes;
		this.categoryDistribution = categoryDistribution;
		this.aiAdoptionRate = aiAdoptionRate;
	}

	public long getTodayTotal() {
		return todayTotal;
	}

	public long getPendingCount() {
		return pendingCount;
	}

	public double getAvgResponseMinutes() {
		return avgResponseMinutes;
	}

	public Map<String, Double> getCategoryDistribution() {
		return categoryDistribution;
	}

	public double getAiAdoptionRate() {
		return aiAdoptionRate;
	}
}