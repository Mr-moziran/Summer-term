package com.project.demo.repository;

import com.project.demo.domain.model.Ticket;
import com.project.demo.domain.enums.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 工单仓储。
 *
 * <p>支持 JpaSpecificationExecutor，用于按状态、分类、提交人和处理人动态组合查询。</p>
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	List<Ticket> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId);

	List<Ticket> findByAssigneeIdAndStatusOrderByCreatedAtDesc(Long assigneeId, TicketStatus status);
}
