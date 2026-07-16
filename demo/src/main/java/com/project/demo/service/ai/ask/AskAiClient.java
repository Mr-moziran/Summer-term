package com.project.demo.service.ai.ask;

import java.util.List;

/**
 * 用户自助问答模型端口。
 */
public interface AskAiClient {

	AskAiDecision decide(String question, List<KnowledgeDocument> documents, AskAiConfidence confidence);
}
