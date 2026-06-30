package com.project.demo.controller;

import com.project.demo.dto.CreateReplyRequest;
import com.project.demo.dto.ReplyResponse;
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

	public ReplyController(TicketWorkflowService ticketWorkflowService) {
		this.ticketWorkflowService = ticketWorkflowService;
	}

	@GetMapping
	public List<ReplyResponse> listReplies(@PathVariable Long ticketId) {
		return ticketWorkflowService.listReplies(ticketId).stream()
				.map(ReplyResponse::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<ReplyResponse> addReply(
			@PathVariable Long ticketId,
			@Valid @RequestBody CreateReplyRequest request) {
		ReplyResponse response = ReplyResponse.from(ticketWorkflowService.addReply(
				ticketId,
				request.getAuthorId(),
				request.getContent(),
				request.isAiAdopted()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
