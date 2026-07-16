package com.project.demo.service.ai.ask;

import com.project.demo.domain.dto.request.EscalationRequest;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 用户自助问答客户端。
 *
 * <p>通过 Spring AI OpenAI 兼容接口调用 DeepSeek，并向模型暴露受控的转人工工具。
 * 工具只表达“需要转人工”的意图；真正创建工单仍由 AskAiService 使用当前登录用户完成。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.provider", havingValue = "deepseek")
public class DeepSeekAskAiClient implements AskAiClient {

	private final ChatClient chatClient;

	public DeepSeekAskAiClient(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@Override
	public AskAiDecision decide(String question, List<KnowledgeDocument> documents, AskAiConfidence confidence) {
		EscalationTool escalationTool = new EscalationTool();
		DeepSeekAskResponse response = chatClient.prompt()
				.system(systemPrompt(confidence))
				.user(userPrompt(question, documents))
				.tools(escalationTool)
				.call()
				.entity(DeepSeekAskResponse.class);
		if (escalationTool.request != null) {
			return AskAiDecision.escalate(escalationTool.request);
		}
		if (response == null) {
			return AskAiDecision.escalate(new EscalationRequest(null, "模型未返回有效结果", summarize(question)));
		}
		if ("ESCALATE".equalsIgnoreCase(response.action())) {
			return AskAiDecision.escalate(new EscalationRequest(
					response.suggestedTitle(),
					firstNonBlank(response.reason(), "模型判断需要转人工"),
					firstNonBlank(response.questionSummary(), summarize(question))));
		}
		return AskAiDecision.answer(firstNonBlank(response.answer(), ""));
	}

	private String systemPrompt(AskAiConfidence confidence) {
		return """
				你是客服自助问答助手。必须遵守：
				1. 只能依据用户消息中提供的知识库片段回答，不得编造片段外事实、政策、步骤或承诺。
				2. 用户明确要求转人工、找客服、人工处理、投诉，或资料不足以支撑回答时，调用 createHumanTicket 工具。
				3. 如果置信度为 MEDIUM，可以基于资料回答，但不要隐瞒资料可能不完整。
				4. 最终只返回 JSON：{"action":"ANSWER|ESCALATE","answer":"...","suggestedTitle":"...","reason":"...","questionSummary":"..."}。
				5. 工具调用结果不能伪装成普通回答。
				当前置信度：%s
				""".formatted(confidence);
	}

	private String userPrompt(String question, List<KnowledgeDocument> documents) {
		StringBuilder prompt = new StringBuilder()
				.append("【用户问题】\n")
				.append(question)
				.append("\n\n【知识库片段】\n");
		if (documents.isEmpty()) {
			prompt.append("无\n");
		}
		else {
			for (KnowledgeDocument document : documents) {
				prompt.append("- 标题：")
						.append(document.title())
						.append("\n  相似度：")
						.append(document.score())
						.append("\n  内容：")
						.append(document.content())
						.append('\n');
			}
		}
		return prompt.toString();
	}

	private String firstNonBlank(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	private String summarize(String question) {
		String trimmed = question.trim();
		if (trimmed.length() <= 60) {
			return trimmed;
		}
		return trimmed.substring(0, 60);
	}

	private record DeepSeekAskResponse(
			String action,
			String answer,
			String suggestedTitle,
			String reason,
			String questionSummary) {
	}

	private static final class EscalationTool {

		private EscalationRequest request;

		@Tool(name = "createHumanTicket", description = "当用户明确要求转人工或知识库资料不足时，调用该工具请求创建人工工单。")
		String createHumanTicket(EscalationToolInput input) {
			this.request = new EscalationRequest(
					input.suggestedTitle(),
					input.reason(),
					input.questionSummary());
			return "ESCALATION_REQUESTED";
		}
	}

	private record EscalationToolInput(String suggestedTitle, String reason, String questionSummary) {
	}
}