package com.project.demo.service.ai.ask;

/**
 * 用户自助问答检索到的知识片段。
 *
 * @param id 知识或历史资料 id
 * @param title 资料标题
 * @param content 资料正文
 * @param score 相似度分数
 */
public record KnowledgeDocument(Long id, String title, String content, double score) {
}
