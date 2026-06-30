package com.project.demo.service;

import com.project.demo.dto.AdminStatsResponse;
import com.project.demo.dto.AgentPerformanceResponse;
import com.project.demo.entity.TicketCategory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStatsService {

	private final JdbcTemplate jdbcTemplate;

	public AdminStatsService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public AdminStatsResponse getStats() {
		long todayTotal = countTodayTickets();
		long pendingCount = countPendingTickets();
		double avgResponseMinutes = averageFirstResponseMinutes();
		Map<String, Double> categoryDistribution = categoryDistribution();
		double aiAdoptionRate = aiAdoptionRate();
		return new AdminStatsResponse(
				todayTotal,
				pendingCount,
				roundTwoDecimals(avgResponseMinutes),
				categoryDistribution,
				roundTwoDecimals(aiAdoptionRate));
	}

	@Transactional(readOnly = true)
	public List<AgentPerformanceResponse> listAgentPerformance() {
		return jdbcTemplate.query("""
				WITH ticket_stats AS (
					SELECT
						assignee_id AS agent_id,
						COUNT(*) AS assigned_count,
						COUNT(*) FILTER (WHERE status IN ('RESOLVED', 'CLOSED')) AS resolved_count
					FROM ticket
					WHERE assignee_id IS NOT NULL
					GROUP BY assignee_id
				), reply_stats AS (
					SELECT
						author_id AS agent_id,
						COUNT(*) AS reply_count,
						CASE WHEN COUNT(*) = 0 THEN 0
							ELSE SUM(CASE WHEN ai_adopted THEN 1 ELSE 0 END)::double precision / COUNT(*)
						END AS ai_adoption_rate
					FROM reply
					GROUP BY author_id
				), first_reply AS (
					SELECT ticket_id, MIN(created_at) AS created_at
					FROM reply
					GROUP BY ticket_id
				), response_stats AS (
					SELECT
						t.assignee_id AS agent_id,
						AVG(EXTRACT(EPOCH FROM (fr.created_at - t.created_at)) / 60.0) AS avg_response_minutes
					FROM ticket t
					JOIN first_reply fr ON fr.ticket_id = t.id
					WHERE t.assignee_id IS NOT NULL
					GROUP BY t.assignee_id
				)
				SELECT
					u.id AS agent_id,
					u.username AS username,
					COALESCE(ts.assigned_count, 0) AS assigned_count,
					COALESCE(ts.resolved_count, 0) AS resolved_count,
					COALESCE(rs.reply_count, 0) AS reply_count,
					COALESCE(rs.ai_adoption_rate, 0) AS ai_adoption_rate,
					COALESCE(res.avg_response_minutes, 0) AS avg_response_minutes
				FROM users u
				LEFT JOIN ticket_stats ts ON ts.agent_id = u.id
				LEFT JOIN reply_stats rs ON rs.agent_id = u.id
				LEFT JOIN response_stats res ON res.agent_id = u.id
				WHERE u.role = 'AGENT'
					AND (COALESCE(ts.assigned_count, 0) > 0 OR COALESCE(rs.reply_count, 0) > 0)
				ORDER BY COALESCE(ts.resolved_count, 0) DESC, COALESCE(ts.assigned_count, 0) DESC, u.id ASC
				""", (resultSet, rowNum) -> new AgentPerformanceResponse(
				resultSet.getLong("agent_id"),
				resultSet.getString("username"),
				resultSet.getLong("assigned_count"),
				resultSet.getLong("resolved_count"),
				resultSet.getLong("reply_count"),
				roundTwoDecimals(resultSet.getDouble("ai_adoption_rate")),
				roundTwoDecimals(resultSet.getDouble("avg_response_minutes"))));
	}

	private long countTodayTickets() {
		Long count = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM ticket
				WHERE created_at >= date_trunc('day', CURRENT_TIMESTAMP)
				""", Long.class);
		return zeroIfNull(count);
	}

	private long countPendingTickets() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ticket WHERE status = 'PENDING'", Long.class);
		return zeroIfNull(count);
	}

	private double averageFirstResponseMinutes() {
		Double average = jdbcTemplate.queryForObject("""
				SELECT AVG(EXTRACT(EPOCH FROM (first_reply.created_at - ticket.created_at)) / 60.0)
				FROM ticket
				JOIN (
					SELECT ticket_id, MIN(created_at) AS created_at
					FROM reply
					GROUP BY ticket_id
				) first_reply ON first_reply.ticket_id = ticket.id
				""", Double.class);
		return zeroIfNull(average);
	}

	private Map<String, Double> categoryDistribution() {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (TicketCategory category : TicketCategory.values()) {
			counts.put(category.name(), 0L);
		}
		jdbcTemplate.query("""
				SELECT category, COUNT(*) AS total
				FROM ticket
				WHERE category IS NOT NULL
				GROUP BY category
				""", (RowCallbackHandler) resultSet -> counts.put(
				resultSet.getString("category"),
				resultSet.getLong("total")));

		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		Map<String, Double> distribution = new LinkedHashMap<>();
		for (var entry : counts.entrySet()) {
			double ratio = total == 0 ? 0.0 : (double) entry.getValue() / total;
			distribution.put(entry.getKey(), roundTwoDecimals(ratio));
		}
		return distribution;
	}

	private double aiAdoptionRate() {
		Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reply", Long.class);
		if (zeroIfNull(total) == 0) {
			return 0.0;
		}
		Long adopted = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reply WHERE ai_adopted = TRUE", Long.class);
		return (double) zeroIfNull(adopted) / total;
	}

	private long zeroIfNull(Long value) {
		return value == null ? 0L : value;
	}

	private double zeroIfNull(Double value) {
		return value == null ? 0.0 : value;
	}

	private double roundTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
