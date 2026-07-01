package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 空相似检索实现。
 *
 * <p>默认启用，返回空列表，保证未配置 PgVector 时 AI 建议接口仍可稳定响应。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.similarity.provider", havingValue = "none", matchIfMissing = true)
public class NoopSimilarTicketSearch implements SimilarTicketSearch {

	@Override
	public List<SimilarTicketContext> search(Ticket ticket) {
		return List.of();
	}
}
