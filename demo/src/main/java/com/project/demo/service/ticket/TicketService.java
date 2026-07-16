package com.project.demo.service.ticket;

import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.enums.TicketCategory;
import com.project.demo.domain.enums.TicketStatus;
import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserRole;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工单基础服务。
 *
 * <p>负责工单创建、查询、列表过滤和用户评分，重点处理不同角色在工单可见性上的差异。</p>
 */
@Service
public class TicketService {

	private static final Logger log = LoggerFactory.getLogger(TicketService.class);

	private final TicketRepository ticketRepository;

	private final UserRepository userRepository;

	public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
	}

	/**
	 * 创建工单。
	 *
	 * <p>普通用户只能以自己身份提交；管理员或客服代建时允许指定提交人，但提交人必须存在。</p>
	 */
	@Transactional
	public Ticket createTicket(User currentUser, Long submitterId, String title, String description) {
		return createTicket(currentUser, submitterId, title, description, true);
	}

	@Transactional
	public Ticket createInternalTicket(User currentUser, Long submitterId, String title, String description) {
		return createTicket(currentUser, submitterId, title, description, false);
	}

	private Ticket createTicket(User currentUser, Long submitterId, String title, String description, boolean visibleToUser) {
		if (currentUser.getRole() == UserRole.USER && !currentUser.getId().equals(submitterId)) {
			throw new AccessDeniedException("权限不足");
		}
		User submitter = userRepository.findById(submitterId)
				.orElseThrow(() -> new ResourceNotFoundException("提交人不存在: " + submitterId));
		Ticket ticket = new Ticket(title.trim(), description.trim(), submitter);
		if (!visibleToUser) {
			ticket.hideFromSubmitter();
		}
		Ticket savedTicket = ticketRepository.save(ticket);
		log.info("工单创建成功: ticketId={}, submitterId={}, visibleToUser={}",
				savedTicket.getId(), submitterId, savedTicket.isVisibleToUser());
		return savedTicket;
	}

	@Transactional(readOnly = true)
	public Ticket getTicket(User currentUser, Long id) {
		Ticket ticket = ticketRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + id));
		ensureCanReadTicket(currentUser, ticket);
		return ticket;
	}

	/**
	 * 按角色安全地查询工单列表。
	 *
	 * <p>USER 强制限定为自己的提交工单；AGENT 强制限定为分配给自己的工单；ADMIN 可按任意条件过滤。</p>
	 */
	@Transactional(readOnly = true)
	public Page<Ticket> listTickets(
			User currentUser,
			TicketStatus status,
			TicketCategory category,
			Long submitterId,
			Long assigneeId,
			Pageable pageable) {
		Long effectiveSubmitterId = submitterId;
		Long effectiveAssigneeId = assigneeId;
		boolean visibleToUserOnly = false;
		if (currentUser.getRole() == UserRole.USER) {
			if (submitterId != null && !currentUser.getId().equals(submitterId)) {
				throw new AccessDeniedException("权限不足");
			}
			if (assigneeId != null) {
				throw new AccessDeniedException("权限不足");
			}
			effectiveSubmitterId = currentUser.getId();
			visibleToUserOnly = true;
		}
		else if (currentUser.getRole() == UserRole.AGENT) {
			if (assigneeId != null && !currentUser.getId().equals(assigneeId)) {
				throw new AccessDeniedException("权限不足");
			}
			if (submitterId != null) {
				throw new AccessDeniedException("权限不足");
			}
			effectiveAssigneeId = currentUser.getId();
		}
		return ticketRepository.findAll(
				buildSpecification(status, category, effectiveSubmitterId, effectiveAssigneeId, visibleToUserOnly),
				pageable);
	}

	@Transactional
	public Ticket rateTicket(User currentUser, Long ticketId, Short rating, String comment) {
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + ticketId));
		if (!currentUser.getId().equals(ticket.getSubmitter().getId()) || !ticket.isVisibleToUser()) {
			throw new AccessDeniedException("权限不足");
		}
		ticket.rate(rating, normalizeComment(comment));
		log.info("工单已评价: ticketId={}, userId={}, rating={}", ticketId, currentUser.getId(), rating);
		return ticket;
	}

	private String normalizeComment(String comment) {
		if (comment == null || comment.isBlank()) {
			return null;
		}
		return comment.trim();
	}

	private void ensureCanReadTicket(User currentUser, Ticket ticket) {
		if (currentUser.getRole() == UserRole.ADMIN) {
			return;
		}
		if (currentUser.getRole() == UserRole.USER
				&& currentUser.getId().equals(ticket.getSubmitter().getId())
				&& ticket.isVisibleToUser()) {
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
	 * 构造动态查询条件。
	 *
	 * <p>这里仅拼接过滤谓词，不做权限判断；调用方必须先把角色可见范围转换为有效的 submitterId/assigneeId。</p>
	 */
	private Specification<Ticket> buildSpecification(
			TicketStatus status,
			TicketCategory category,
			Long submitterId,
			Long assigneeId,
			boolean visibleToUserOnly) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (status != null) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status));
			}
			if (category != null) {
				predicates.add(criteriaBuilder.equal(root.get("category"), category));
			}
			if (submitterId != null) {
				predicates.add(criteriaBuilder.equal(root.get("submitter").get("id"), submitterId));
			}
			if (visibleToUserOnly) {
				predicates.add(criteriaBuilder.isTrue(root.get("visibleToUser")));
			}
			if (assigneeId != null) {
				predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}
}
