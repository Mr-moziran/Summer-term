package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.index.provider", havingValue = "none", matchIfMissing = true)
public class NoopResolvedTicketIndex implements ResolvedTicketIndex {

	@Override
	public void index(Ticket ticket, String solution) {
		// Intentionally disabled for local development and tests.
	}
}
