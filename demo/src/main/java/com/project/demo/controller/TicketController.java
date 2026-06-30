package com.project.demo.controller;

import com.project.demo.dto.CreateTicketRequest;
import com.project.demo.dto.PageResponse;
import com.project.demo.dto.TicketResponse;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketStatus;
import com.project.demo.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
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
}
