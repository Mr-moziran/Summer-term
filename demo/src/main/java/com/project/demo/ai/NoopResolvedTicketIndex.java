package com.project.demo.ai;

import com.project.demo.ticket.Ticket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 空已解决工单索引实现。
 *
 * <p>默认启用，不写向量库，避免本地开发和测试依赖 pgvector 表或外部 embedding 服务。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.index.provider", havingValue = "none", matchIfMissing = true)
public class NoopResolvedTicketIndex implements ResolvedTicketIndex {

	@Override
	public void index(Ticket ticket, String solution) {
		// Intentionally disabled for local development and tests.
	}
}
