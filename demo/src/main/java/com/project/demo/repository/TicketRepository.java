package com.project.demo.repository;

import com.project.demo.entity.Ticket;
import com.project.demo.entity.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	List<Ticket> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId);

	List<Ticket> findByAssigneeIdAndStatusOrderByCreatedAtDesc(Long assigneeId, TicketStatus status);
}
