package com.project.demo.service.ai.ask;

/**
 * 首批演示知识库文档的资源格式。
 */
public record KnowledgeBaseSeedDocument(Long id, String title, String category, String content, String source) {
}
