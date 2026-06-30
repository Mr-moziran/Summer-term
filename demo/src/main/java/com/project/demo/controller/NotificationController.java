package com.project.demo.controller;

import com.project.demo.dto.NotificationResponse;
import com.project.demo.dto.PageResponse;
import com.project.demo.entity.User;
import com.project.demo.service.CurrentUserService;
import com.project.demo.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	private final CurrentUserService currentUserService;

	public NotificationController(NotificationService notificationService, CurrentUserService currentUserService) {
		this.notificationService = notificationService;
		this.currentUserService = currentUserService;
	}

	@GetMapping
	public PageResponse<NotificationResponse> listNotifications(
			@RequestParam(defaultValue = "false") boolean unreadOnly,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		User currentUser = currentUserService.getCurrentUser();
		return PageResponse.from(notificationService.listNotifications(currentUser, unreadOnly, pageable)
				.map(NotificationResponse::from));
	}

	@PatchMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id) {
		User currentUser = currentUserService.getCurrentUser();
		return NotificationResponse.from(notificationService.markRead(currentUser, id));
	}
}
