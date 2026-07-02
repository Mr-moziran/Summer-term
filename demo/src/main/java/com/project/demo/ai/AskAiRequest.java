package com.project.demo.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户自助问答请求。
 */
public class AskAiRequest {

	@NotBlank(message = "问题不能为空")
	@Size(max = 1000, message = "问题长度不能超过1000个字符")
	private String question;

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
}