package com.project.demo.service.ai;

import com.project.demo.entity.TicketCategory;
import com.project.demo.entity.TicketPriority;

public record TicketClassification(TicketCategory category, TicketPriority priority, String reason) {
}
