package com.project.demo.service;

import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Reply;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.entity.UserRole;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.ReplyRepository;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import com.project.demo.service.ai.ResolvedTicketIndex;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketWorkflowService {

	private final TicketRepository ticketRepository;

	private final ReplyRepository replyRepository;

	private final UserRepository userRepository;

	private final NotificationService notificationService;

	private final ResolvedTicketIndex resolvedTicketIndex;

	public TicketWorkflowService(
			TicketRepository ticketRepository,
			ReplyRepository replyRepository,
			UserRepository userRepository,
			NotificationService notificationService,
			ResolvedTicketIndex resolvedTicketIndex) {
		this.ticketRepository = ticketRepository;
		this.replyRepository = replyRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
		this.resolvedTicketIndex = resolvedTicketIndex;
	}

	@Transactional(readOnly = true)
	public List<Reply> listReplies(User currentUser, Long ticketId) {
		Ticket ticket = getTicket(ticketId);
		ensureCanReadTicket(currentUser, ticket);
		return replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
	}

	@Transactional
	public Reply addReply(Long ticketId, User currentUser, Long authorId, String content, boolean aiAdopted) {
		Ticket ticket = getTicket(ticketId);
		ensureSameUser(currentUser, authorId);
		ensureCanHandleTicket(currentUser, ticket);
		Reply reply = replyRepository.save(new Reply(ticket, currentUser, content.trim(), false, aiAdopted));
		if (!currentUser.getId().equals(ticket.getSubmitter().getId())) {
			notificationService.createNotification(
					ticket.getSubmitter(),
					NotificationType.NEW_REPLY,
					ticket,
					"您的工单「" + ticket.getTitle() + "」有新的回复");
		}
		return reply;
	}

	@Transactional
	public Ticket assignTicket(Long ticketId, Long assigneeId) {
		Ticket ticket = getTicket(ticketId);
		User assignee = getUser(assigneeId, "客服不存在: ");
		ticket.assignTo(assignee);
		notificationService.createNotification(
				ticket.getSubmitter(),
				NotificationType.ASSIGNED,
				ticket,
				"您的工单「" + ticket.getTitle() + "」已分配给客服 " + assignee.getUsername());
		return ticket;
	}

	@Transactional
	public Ticket updateStatus(User currentUser, Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		ensureCanHandleTicket(currentUser, ticket);
		ticket.changeStatus(status);
		notificationService.createNotification(
				ticket.getSubmitter(),
				NotificationType.STATUS_CHANGE,
				ticket,
				"您的工单「" + ticket.getTitle() + "」状态已更新为 " + status);
		if (status == TicketStatus.RESOLVED) {
			resolvedTicketIndex.index(ticket, latestReplyContent(ticket.getId()));
		}
		return ticket;
	}

	private String latestReplyContent(Long ticketId) {
		List<Reply> replies = replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
		if (replies.isEmpty()) {
			return "";
		}
		return replies.getLast().getContent();
	}

	private void ensureSameUser(User currentUser, Long requestedUserId) {
		if (!currentUser.getId().equals(requestedUserId)) {
			throw new AccessDeniedException("权限不足");
		}
	}

	private void ensureCanReadTicket(User currentUser, Ticket ticket) {
		if (currentUser.getRole() == UserRole.ADMIN) {
			return;
		}
		if (currentUser.getRole() == UserRole.USER
				&& currentUser.getId().equals(ticket.getSubmitter().getId())) {
			return;
		}
		if (currentUser.getRole() == UserRole.AGENT
				&& ticket.getAssignee() != null
				&& currentUser.getId().equals(ticket.getAssignee().getId())) {
			return;
		}
		throw new AccessDeniedException("权限不足");
	}

	private void ensureCanHandleTicket(User currentUser, Ticket ticket) {
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

	private Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + ticketId));
	}

	private User getUser(Long userId, String messagePrefix) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException(messagePrefix + userId));
	}
}
