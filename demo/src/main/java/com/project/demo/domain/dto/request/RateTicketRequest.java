package com.project.demo.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 工单评分请求 DTO，包含 1 到 5 分评分和可选评价内容。
 */
public class RateTicketRequest {

	@NotNull(message = "评分不能为空")
	@Min(value = 1, message = "评分不能低于1分")
	@Max(value = 5, message = "评分不能高于5分")
	private Short rating;

	@Size(max = 1000, message = "评价内容不能超过1000个字符")
	private String comment;

	public Short getRating() {
		return rating;
	}

	public void setRating(Short rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
