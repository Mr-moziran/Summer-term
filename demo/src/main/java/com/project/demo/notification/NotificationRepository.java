package com.project.demo.notification;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 通知仓储。
 *
 * <p>围绕当前用户通知列表、未读筛选和未读数量统计提供查询方法。</p>
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

	Page<Notification> findByUserId(Long userId, Pageable pageable);

	Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(Long userId);
}
