package com.project.demo.service;

import com.project.demo.dto.AdminStatsResponse;
import com.project.demo.entity.TicketCategory;
import java.util.LinkedHashMap;
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