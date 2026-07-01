package com.project.demo.controller;

import com.project.demo.dto.AssignTicketRequest;
import com.project.demo.dto.CreateTicketRequest;
import com.project.demo.dto.PageResponse;
import com.project.demo.dto.RateTicketRequest;
import com.project.demo.dto.TicketResponse;
import com.project.demo.dto.UpdateTicketStatusRequest;
import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketStatus;
import com.project.demo.entity.User;
import com.project.demo.service.CurrentUserService;
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

/**
 * 工单 REST 接口控制器。
 *
 * <p>承接工单创建、查询、分配、状态更新和评分等 HTTP 请求，具体权限和业务规则委托给服务层。</p>
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;

	private final TicketWorkflowService ticketWorkflowService;

	private final CurrentUserService currentUserService;

	public TicketController(
			TicketService ticketService,
			TicketWorkflowService ticketWorkflowService,
			CurrentUserService currentUserService) {
		this.ticketService = ticketService;
		this.ticketWorkflowService = ticketWorkflowService;
		this.currentUserService = currentUserService;
	}

	@PostMapping
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		TicketResponse response = TicketResponse.from(ticketService.createTicket(
				currentUser,
				request.getSubmitterId(),
				request.getTitle(),
				request.getDescription()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public PageResponse<TicketResponse> listTickets(
			@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) TicketCategory category,
			@RequestParam(required = false) Long submitterId,
			@RequestParam(required = false) Long assigneeId,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		User currentUser = currentUserService.getCurrentUser();
		return PageResponse.from(ticketService.listTickets(currentUser, status, category, submitterId, assigneeId, pageable)
				.map(TicketResponse::from));
	}

	@GetMapping("/{id}")
	public TicketResponse getTicket(@PathVariable Long id) {
		User currentUser = currentUserService.getCurrentUser();
		return TicketResponse.from(ticketService.getTicket(currentUser, id));
	}

	@PostMapping("/{id}/assign")
	public TicketResponse assignTicket(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
		return TicketResponse.from(ticketWorkflowService.assignTicket(id, request.getAssigneeId()));
	}

	@PatchMapping("/{id}/status")
	public TicketResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTicketStatusRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		return TicketResponse.from(ticketWorkflowService.updateStatus(currentUser, id, request.getStatus()));
	}

	@PostMapping("/{id}/rate")
	public TicketResponse rateTicket(@PathVariable Long id, @Valid @RequestBody RateTicketRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		return TicketResponse.from(ticketService.rateTicket(currentUser, id, request.getRating(), request.getComment()));
	}
}
