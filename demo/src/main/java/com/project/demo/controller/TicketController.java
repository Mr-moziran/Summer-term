package com.project.demo.controller;

import com.project.demo.dto.AssignTicketRequest;
import com.project.demo.dto.CreateTicketRequest;
import com.project.demo.dto.PageResponse;
import com.project.demo.dto.TicketResponse;
import com.project.demo.dto.UpdateTicketStatusRequest;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketStatus;
import com.project.demo.service.TicketService;
import com.project.demo.service.TicketWorkflowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;

	private final TicketWorkflowService ticketWorkflowService;

	public TicketController(TicketService ticketService, TicketWorkflowService ticketWorkflowService) {
		this.ticketService = ticketService;
		this.ticketWorkflowService = ticketWorkflowService;
	}

	@PostMapping
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		TicketResponse response = TicketResponse.from(
				ticketService.createTicket(request.getSubmitterId(), request.getTitle(), request.getDescription()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public PageResponse<TicketResponse> listTickets(
			@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) TicketCategory category,
			@RequestParam(required = false) Long submitterId,
			@RequestParam(required = false) Long assigneeId,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponse.from(ticketService.listTickets(status, category, submitterId, assigneeId, pageable)
				.map(TicketResponse::from));
	}

	@GetMapping("/{id}")
	public TicketResponse getTicket(@PathVariable Long id) {
		return TicketResponse.from(ticketService.getTicket(id));
	}

	@PostMapping("/{id}/assign")
	public TicketResponse assignTicket(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
		return TicketResponse.from(ticketWorkflowService.assignTicket(id, request.getAssigneeId()));
	}

	@PatchMapping("/{id}/status")
	public TicketResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTicketStatusRequest request) {
		return TicketResponse.from(ticketWorkflowService.updateStatus(id, request.getStatus()));
	}
}
