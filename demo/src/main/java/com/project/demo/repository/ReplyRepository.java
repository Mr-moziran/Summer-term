package com.project.demo.repository;

import com.project.demo.entity.Reply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

	List<Reply> findByTicketId(Long ticketId);

	List<Reply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
