package com.project.demo.controller;

import com.project.demo.dto.AiSuggestionResponse;
import com.project.demo.service.AiSuggestionService;
import com.project.demo.service.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 建议接口控制器。
 *
 * <p>客服或管理员打开工单处理页时调用该接口，同步返回 AI 回复草稿和相似历史工单。</p>
 */
@RestController
@RequestMapping("/api/ai/suggest")
public class AiSuggestionController {

	private final AiSuggestionService aiSuggestionService;

	private final CurrentUserService currentUserService;

	public AiSuggestionController(AiSuggestionService aiSuggestionService, CurrentUserService currentUserService) {
		this.aiSuggestionService = aiSuggestionService;
		this.currentUserService = currentUserService;
	}

	@GetMapping("/{ticketId}")
	public AiSuggestionResponse suggest(@PathVariable Long ticketId) {
		return aiSuggestionService.suggest(currentUserService.getCurrentUser(), ticketId);
	}
}