package com.project.demo.ai;

import com.project.demo.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户自助问答接口。
 *
 * <p>普通登录用户可通过该入口先查询知识库，无法解决或明确要求人工时自动转人工工单。</p>
 */
@RestController
@RequestMapping("/api/ai/ask")
public class AskAiController {

	private final AskAiService askAiService;

	private final CurrentUserService currentUserService;

	public AskAiController(AskAiService askAiService, CurrentUserService currentUserService) {
		this.askAiService = askAiService;
		this.currentUserService = currentUserService;
	}

	@PostMapping
	public AskAiResponse ask(@Valid @RequestBody AskAiRequest request) {
		return askAiService.ask(currentUserService.getCurrentUser(), request.getQuestion());
	}
}