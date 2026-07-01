package com.project.demo.ticket;

import com.project.demo.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 工单实体。
 *
 * <p>工单是系统核心聚合，记录提交人、处理客服、分类、优先级、状态流转、评分和关键时间点。
 * 实体方法只封装自身状态变化，跨聚合通知和权限判断由 service 层完成。</p>
 */
@Entity
@Table(name = "ticket")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TicketCategory category;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private TicketPriority priority;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TicketStatus status = TicketStatus.PENDING;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submitter_id", nullable = false)
	private User submitter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Column(name = "ai_classified", nullable = false)
	private boolean aiClassified;

	private Short rating;

	@Column(name = "rating_comment", columnDefinition = "TEXT")
	private String ratingComment;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "resolved_at")
	private OffsetDateTime resolvedAt;

	@Column(name = "closed_at")
	private OffsetDateTime closedAt;

	protected Ticket() {
	}

	public Ticket(String title, String description, User submitter) {
		this.title = title;
		this.description = description;
		this.submitter = submitter;
	}

	public void assignTo(User assignee) {
		this.assignee = assignee;
		this.status = TicketStatus.ASSIGNED;
	}

	public void changeStatus(TicketStatus status) {
		this.status = status;
		OffsetDateTime now = OffsetDateTime.now();
		if (status == TicketStatus.RESOLVED && this.resolvedAt == null) {
			this.resolvedAt = now;
		}
		if (status == TicketStatus.CLOSED && this.closedAt == null) {
			this.closedAt = now;
		}
	}

	public void rate(Short rating, String ratingComment) {
		this.rating = rating;
		this.ratingComment = ratingComment;
	}

	public void applyAiClassification(TicketCategory category, TicketPriority priority) {
		this.category = category;
		this.priority = priority;
		this.aiClassified = true;
	}

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) {
			this.status = TicketStatus.PENDING;
		}
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TicketCategory getCategory() {
		return category;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public User getSubmitter() {
		return submitter;
	}

	public User getAssignee() {
		return assignee;
	}

	public boolean isAiClassified() {
		return aiClassified;
	}

	public Short getRating() {
		return rating;
	}

	public String getRatingComment() {
		return ratingComment;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public OffsetDateTime getResolvedAt() {
		return resolvedAt;
	}

	public OffsetDateTime getClosedAt() {
		return closedAt;
	}
}
