package com.project.demo.admin;

/**
 * 客服绩效响应 DTO，汇总单个客服的分配量、解决量、回复量、AI 采纳率和平均响应时间。
 */
public class AgentPerformanceResponse {

	private final Long agentId;
	private final String username;
	private final long assignedCount;
	private final long resolvedCount;
	private final long replyCount;
	private final double aiAdoptionRate;
	private final double avgResponseMinutes;

	public AgentPerformanceResponse(
			Long agentId,
			String username,
			long assignedCount,
			long resolvedCount,
			long replyCount,
			double aiAdoptionRate,
			double avgResponseMinutes) {
		this.agentId = agentId;
		this.username = username;
		this.assignedCount = assignedCount;
		this.resolvedCount = resolvedCount;
		this.replyCount = replyCount;
		this.aiAdoptionRate = aiAdoptionRate;
		this.avgResponseMinutes = avgResponseMinutes;
	}

	public Long getAgentId() {
		return agentId;
	}

	public String getUsername() {
		return username;
	}

	public long getAssignedCount() {
		return assignedCount;
	}

	public long getResolvedCount() {
		return resolvedCount;
	}

	public long getReplyCount() {
		return replyCount;
	}

	public double getAiAdoptionRate() {
		return aiAdoptionRate;
	}

	public double getAvgResponseMinutes() {
		return avgResponseMinutes;
	}
}
