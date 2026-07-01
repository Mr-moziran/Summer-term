package com.project.demo.repository;

import com.project.demo.entity.Reply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 回复仓储。
 *
 * <p>按工单时间顺序读取回复，用于详情页时间线和解决方案索引。</p>
 */
public interface ReplyRepository extends JpaRepository<Reply, Long> {

	List<Reply> findByTicketId(Long ticketId);

	List<Reply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
