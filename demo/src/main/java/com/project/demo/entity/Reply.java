package com.project.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reply")
public class Reply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_ai_draft", nullable = false)
	private boolean aiDraft;

	@Column(name = "ai_adopted", nullable = false)
	private boolean aiAdopted;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected Reply() {
	}

	public Reply(Ticket ticket, User author, String content, boolean aiDraft, boolean aiAdopted) {
		this.ticket = ticket;
		this.author = author;
		this.content = content;
		this.aiDraft = aiDraft;
		this.aiAdopted = aiAdopted;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public User getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}

	public boolean isAiDraft() {
		return aiDraft;
	}

	public boolean isAiAdopted() {
		return aiAdopted;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
