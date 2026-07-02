package com.project.demo.ai;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 空知识库检索实现。
 *
 * <p>默认启用，保证未配置 PgVector 知识检索时自助问答仍能走转人工兜底链路。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.knowledge.provider", havingValue = "none", matchIfMissing = true)
public class NoopKnowledgeSearch implements KnowledgeSearch {

	@Override
	public List<KnowledgeDocument> search(String question, int topK, double similarityThreshold) {
		return List.of();
	}
}
