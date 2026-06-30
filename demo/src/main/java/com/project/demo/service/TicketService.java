package com.project.demo.service;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.TicketRepository;
import com.project.demo.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;

	private final UserRepository userRepository;

	public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public Ticket createTicket(Long submitterId, String title, String description) {
		User submitter = userRepository.findById(submitterId)
				.orElseThrow(() -> new ResourceNotFoundException("提交人不存在: " + submitterId));
		return ticketRepository.save(new Ticket(title.trim(), description.trim(), submitter));
	}

	@Transactional(readOnly = true)
	public Ticket getTicket(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("工单不存在: " + id));
	}

	@Transactional(readOnly = true)
	public Page<Ticket> listTickets(
			TicketStatus status,
			TicketCategory category,
			Long submitterId,
			Long assigneeId,
			Pageable pageable) {
		return ticketRepository.findAll(buildSpecification(status, category, submitterId, assigneeId), pageable);
	}

	private Specification<Ticket> buildSpecification(
			TicketStatus status,
			TicketCategory category,
			Long submitterId,
			Long assigneeId) {
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
			if (assigneeId != null) {
				predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}
}
