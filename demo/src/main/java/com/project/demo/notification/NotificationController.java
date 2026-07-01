package com.project.demo.notification;

import com.project.demo.common.dto.PageResponse;
import com.project.demo.user.User;
import com.project.demo.security.CurrentUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知接口控制器。
 *
 * <p>提供当前用户通知列表、未读数量和标记已读能力，用于前端消息中心和通知角标。</p>
 */
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

	@GetMapping("/unread-count")
	public UnreadNotificationCountResponse countUnread() {
		User currentUser = currentUserService.getCurrentUser();
		return new UnreadNotificationCountResponse(notificationService.countUnread(currentUser));
	}

	@PatchMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id) {
		User currentUser = currentUserService.getCurrentUser();
		return NotificationResponse.from(notificationService.markRead(currentUser, id));
	}
}
