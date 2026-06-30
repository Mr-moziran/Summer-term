package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.similarity.provider", havingValue = "none", matchIfMissing = true)
public class NoopSimilarTicketSearch implements SimilarTicketSearch {

	@Override
	public List<SimilarTicketContext> search(Ticket ticket) {
		return List.of();
	}
}
