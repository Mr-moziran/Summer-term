package com.project.demo.service;

import com.project.demo.entity.Notification;
import com.project.demo.entity.NotificationType;
import com.project.demo.entity.Ticket;
import com.project.demo.entity.User;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	private final NotificationPushService notificationPushService;

	public NotificationService(NotificationRepository notificationRepository, NotificationPushService notificationPushService) {
		this.notificationRepository = notificationRepository;
		this.notificationPushService = notificationPushService;
	}

	@Transactional
	public Notification createNotification(User user, NotificationType type, Ticket ticket, String message) {
		Notification notification = notificationRepository.save(new Notification(user, type, ticket, message));
		notificationPushService.push(notification);
		return notification;
	}

	@Transactional(readOnly = true)
	public Page<Notification> listNotifications(User currentUser, boolean unreadOnly, Pageable pageable) {
		if (unreadOnly) {
			return notificationRepository.findByUserIdAndIsReadFalse(currentUser.getId(), pageable);
		}
		return notificationRepository.findByUserId(currentUser.getId(), pageable);
	}

	@Transactional(readOnly = true)
	public long countUnread(User currentUser) {
		return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
	}

	@Transactional
	public Notification markRead(User currentUser, Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ResourceNotFoundException("通知不存在: " + notificationId));
		if (!currentUser.getId().equals(notification.getUser().getId())) {
			throw new AccessDeniedException("权限不足");
		}
		notification.markRead();
		return notification;
	}
}
