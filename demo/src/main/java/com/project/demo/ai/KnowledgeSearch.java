package com.project.demo.ai;

import java.util.List;

/**
 * 用户自助问答知识检索端口。
 */
public interface KnowledgeSearch {

	List<KnowledgeDocument> search(String question, int topK, double similarityThreshold);
}
