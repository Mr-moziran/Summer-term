package com.project.demo.ai;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地自助问答模型实现。
 *
 * <p>用于开发和测试环境，不调用外部 LLM；它只拼接检索到的知识片段，并识别明确转人工意图。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.provider", havingValue = "local", matchIfMissing = true)
public class LocalAskAiClient implements AskAiClient {

	@Override
	public AskAiDecision decide(String question, List<KnowledgeDocument> documents, AskAiConfidence confidence) {
		if (hasExplicitEscalationIntent(question)) {
			return AskAiDecision.escalate(new EscalationRequest(
					null,
					"用户明确要求转人工",
					summarize(question)));
		}
		if (documents.isEmpty()) {
			return AskAiDecision.escalate(new EscalationRequest(
					null,
					"知识库资料不足，无法可靠回答",
					summarize(question)));
		}
		return AskAiDecision.answer(answerFrom(documents));
	}

	private String answerFrom(List<KnowledgeDocument> documents) {
		StringBuilder answer = new StringBuilder("根据知识库资料：");
		for (KnowledgeDocument document : documents) {
			answer.append("\n- ")
					.append(document.title())
					.append("：")
					.append(document.content());
		}
		return answer.toString();
	}

	private boolean hasExplicitEscalationIntent(String question) {
		return question.contains("转人工")
				|| question.contains("找客服")
				|| question.contains("人工处理")
				|| question.contains("人工客服")
				|| question.contains("没解决")
				|| question.contains("我要投诉");
	}

	private String summarize(String question) {
		String trimmed = question.trim();
		if (trimmed.length() <= 60) {
			return trimmed;
		}
		return trimmed.substring(0, 60);
	}
}
