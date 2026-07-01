package com.project.demo.reply;

import com.project.demo.user.User;
import com.project.demo.security.CurrentUserService;
import com.project.demo.ticket.TicketWorkflowService;
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

/**
 * 工单回复接口控制器。
 *
 * <p>负责读取和新增某个工单下的回复，服务层会继续校验当前用户是否有权查看或处理该工单。</p>
 */
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
