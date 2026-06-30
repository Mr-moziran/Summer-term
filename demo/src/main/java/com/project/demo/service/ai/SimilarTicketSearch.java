package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;

public interface SimilarTicketSearch {

	List<SimilarTicketContext> search(Ticket ticket);
}
