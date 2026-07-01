package com.project.demo.service;

import com.project.demo.dto.AiSuggestionResponse;
import com.project.demo.dto.SimilarTicketResponse;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.TicketRepository;
import com.project.demo.service.ai.SimilarTicketContext;
import com.project.demo.service.ai.SimilarTicketSearch;
import com.project.demo.service.ai.TicketAiClient;
import com.project.demo.service.ai.TicketClassification;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 建议编排服务。
 *
 * <p>客服打开工单时同步完成权限校验、必要分类、相似历史工单检索和回复草稿生成。
 * 具体 AI 与向量实现通过 service.ai 端口注入，默认不依赖外部服务。</p>
 */
@Service
public class AiSuggestionService {

	private final TicketRepository ticketRepository;
	private final TicketAiClient ticketAiClient;
	private final SimilarTicketSearch similarTicketSearch;

	public AiSuggestionService(
			TicketRepository ticketRepository,
			TicketAiClient ticketAiClient,
			SimilarTicketSearch similarTicketSearch) {
		this.ticketRepository = ticketRepository;
		this.ticketAiClient = ticketAiClient;
		this.similarTicketSearch = similarTicketSearch;
	}

	/**
	 * 生成客服处理建议。
	 *
	 * <p>这是 AI 流程的同步入口：先确认当前客服或管理员有权查看该工单，再按需分类，随后检索相似历史案例，
	 * 最后由 AI 客户端生成可编辑的回复草稿。</p>
	 */
	@Transactional
	public AiSuggestionResponse suggest(User currentUser, Long ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + ticketId));
		ensureCanSuggest(currentUser, ticket);
		classifyIfNeeded(ticket);

		List<SimilarTicketContext> similarTickets = similarTicketSearch.search(ticket);
		String draft = ticketAiClient.draftReply(ticket, similarTickets);
		return new AiSuggestionResponse(draft, toResponse(similarTickets));
	}

	private void ensureCanSuggest(User currentUser, Ticket ticket) {
		if (currentUser.getRole() == UserRole.ADMIN) {
			return;
		}
		if (currentUser.getRole() == UserRole.AGENT
				&& ticket.getAssignee() != null
				&& currentUser.getId().equals(ticket.getAssignee().getId())) {
			return;
		}
		throw new AccessDeniedException("权限不足");
	}

	/**
	 * 仅对未分类工单调用 AI 分类，避免重复覆盖人工或既有分类结果。
	 */
	private void classifyIfNeeded(Ticket ticket) {
		if (ticket.isAiClassified()) {
			return;
		}
		TicketClassification classification = ticketAiClient.classify(ticket);
		ticket.applyAiClassification(classification.category(), classification.priority());
	}

	private List<SimilarTicketResponse> toResponse(List<SimilarTicketContext> similarTickets) {
		return similarTickets.stream()
				.map(ticket -> new SimilarTicketResponse(
						ticket.ticketId(),
						ticket.title(),
						ticket.solution(),
						ticket.score()))
				.toList();
	}
}
