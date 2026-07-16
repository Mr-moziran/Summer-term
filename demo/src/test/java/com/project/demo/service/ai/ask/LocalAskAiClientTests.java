package com.project.demo.service.ai.ask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LocalAskAiClientTests {

	private final LocalAskAiClient client = new LocalAskAiClient();

	@Test
	void answersFromKnowledgeDocumentsOnly() {
		AskAiDecision decision = client.decide(
				"忘记密码怎么办",
				List.of(new KnowledgeDocument(1L, "账号帮助", "请在登录页点击忘记密码。", 0.91)),
				AskAiConfidence.HIGH);

		assertThat(decision.requiresEscalation()).isFalse();
		assertThat(decision.getAnswer()).contains("账号帮助");
		assertThat(decision.getAnswer()).contains("请在登录页点击忘记密码。");
	}

	@Test
	void requestsEscalationForExplicitHumanTransferWords() {
		AskAiDecision decision = client.decide(
				"我还是没解决，转人工",
				List.of(new KnowledgeDocument(1L, "账号帮助", "请在登录页点击忘记密码。", 0.91)),
				AskAiConfidence.HIGH);

		assertThat(decision.requiresEscalation()).isTrue();
		assertThat(decision.getEscalationRequest().reason()).contains("明确要求转人工");
		assertThat(decision.getEscalationRequest().questionSummary()).contains("转人工");
	}
}
