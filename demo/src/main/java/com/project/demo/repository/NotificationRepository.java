package com.project.demo.repository;

import com.project.demo.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

	Page<Notification> findByUserId(Long userId, Pageable pageable);

	Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(Long userId);
}
