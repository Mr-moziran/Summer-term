package com.project.demo.service;

import com.project.demo.entity.Notification;
import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Reply;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.NotificationRepository;
import com.project.demo.repository.ReplyRepository;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketWorkflowService {

	private final TicketRepository ticketRepository;

	private final ReplyRepository replyRepository;

	private final UserRepository userRepository;

	private final NotificationRepository notificationRepository;

	public TicketWorkflowService(
			TicketRepository ticketRepository,
			ReplyRepository replyRepository,
			UserRepository userRepository,
			NotificationRepository notificationRepository) {
		this.ticketRepository = ticketRepository;
		this.replyRepository = replyRepository;
		this.userRepository = userRepository;
		this.notificationRepository = notificationRepository;
	}

	@Transactional(readOnly = true)
	public List<Reply> listReplies(Long ticketId) {
		ensureTicketExists(ticketId);
		return replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
	}

	@Transactional
	public Reply addReply(Long ticketId, Long authorId, String content, boolean aiAdopted) {
		Ticket ticket = getTicket(ticketId);
		User author = getUser(authorId, "回复作者不存在: ");
		Reply reply = replyRepository.save(new Reply(ticket, author, content.trim(), false, aiAdopted));
		if (!author.getId().equals(ticket.getSubmitter().getId())) {
			notificationRepository.save(new Notification(
					ticket.getSubmitter(),
					NotificationType.NEW_REPLY,
					ticket,
					"您的工单「" + ticket.getTitle() + "」有新的回复"));
		}
		return reply;
	}

	@Transactional
	public Ticket assignTicket(Long ticketId, Long assigneeId) {
		Ticket ticket = getTicket(ticketId);
		User assignee = getUser(assigneeId, "客服不存在: ");
		ticket.assignTo(assignee);
		notificationRepository.save(new Notification(
				ticket.getSubmitter(),
				NotificationType.ASSIGNED,
				ticket,
				"您的工单「" + ticket.getTitle() + "」已分配给客服 " + assignee.getUsername()));
		return ticket;
	}

	@Transactional
	public Ticket updateStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		ticket.changeStatus(status);
		notificationRepository.save(new Notification(
				ticket.getSubmitter(),
				NotificationType.STATUS_CHANGE,
				ticket,
				"您的工单「" + ticket.getTitle() + "」状态已更新为 " + status));
		return ticket;
	}

	private void ensureTicketExists(Long ticketId) {
		if (!ticketRepository.existsById(ticketId)) {
			throw new ResourceNotFoundException("工单不存在: " + ticketId);
		}
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
