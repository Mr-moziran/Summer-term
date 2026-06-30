package com.project.demo.service;

import com.project.demo.dto.AiSuggestionResponse;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.TicketRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiSuggestionService {

	private final TicketRepository ticketRepository;

	public AiSuggestionService(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Transactional(readOnly = true)
	public AiSuggestionResponse suggest(User currentUser, Long ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + ticketId));
		ensureCanSuggest(currentUser, ticket);
		return new AiSuggestionResponse(buildDraft(ticket), List.of());
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

	private String buildDraft(Ticket ticket) {
		return "您好，关于工单「" + ticket.getTitle() + "」，我们已收到您的问题："
				+ ticket.getDescription()
				+ "。建议先核对问题发生时间、操作步骤和相关截图，客服会根据这些信息继续处理。";
	}
}