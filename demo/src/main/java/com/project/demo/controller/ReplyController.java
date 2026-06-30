package com.project.demo.controller;

import com.project.demo.dto.CreateReplyRequest;
import com.project.demo.dto.ReplyResponse;
import com.project.demo.entity.User;
import com.project.demo.service.CurrentUserService;
import com.project.demo.service.TicketWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{ticketId}/replies")
public class ReplyController {

	private final TicketWorkflowService ticketWorkflowService;

	private final CurrentUserService currentUserService;

	public ReplyController(TicketWorkflowService ticketWorkflowService, CurrentUserService currentUserService) {
		this.ticketWorkflowService = ticketWorkflowService;
		this.currentUserService = currentUserService;
	}

	@GetMapping
	public List<ReplyResponse> listReplies(@PathVariable Long ticketId) {
		User currentUser = currentUserService.getCurrentUser();
		return ticketWorkflowService.listReplies(currentUser, ticketId).stream()
				.map(ReplyResponse::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<ReplyResponse> addReply(
			@PathVariable Long ticketId,
			@Valid @RequestBody CreateReplyRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		ReplyResponse response = ReplyResponse.from(ticketWorkflowService.addReply(
				ticketId,
				currentUser,
				request.getAuthorId(),
				request.getContent(),
				request.isAiAdopted()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
