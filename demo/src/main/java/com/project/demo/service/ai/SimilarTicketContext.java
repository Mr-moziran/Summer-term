package com.project.demo.service.ai;

public record SimilarTicketContext(Long ticketId, String title, String solution, double score) {
}
