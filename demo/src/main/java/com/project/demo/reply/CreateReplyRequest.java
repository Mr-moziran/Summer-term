package com.project.demo.reply;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 新增回复请求 DTO，包含作者、回复内容和是否采纳 AI 草稿。
 */
public class CreateReplyRequest {

	@NotNull(message = "回复作者ID不能为空")
	private Long authorId;

	@NotBlank(message = "回复内容不能为空")
	private String content;

	private boolean aiAdopted;

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isAiAdopted() {
		return aiAdopted;
	}

	public void setAiAdopted(boolean aiAdopted) {
		this.aiAdopted = aiAdopted;
	}
}
