package com.project.demo.service.ai.ask;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 将 classpath 中维护的演示 FAQ 写入向量知识库。
 *
 * <p>仅在显式开启开关时执行。每次导入前都会清理旧的知识库文档，因此可重复执行，且不会影响已解决工单索引。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.knowledge.seed.enabled", havingValue = "true")
public class KnowledgeBaseSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseSeeder.class);

	private static final String RESOURCE_PATH = "db/knowledge-base.json";

	private final VectorStore vectorStore;

	private final ObjectMapper objectMapper;

	public KnowledgeBaseSeeder(VectorStore vectorStore, ObjectMapper objectMapper) {
		this.vectorStore = vectorStore;
		this.objectMapper = objectMapper;
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		List<KnowledgeBaseSeedDocument> documents = readDocuments();
		vectorStore.delete("documentType == 'knowledge'");
		vectorStore.add(documents.stream().map(this::toVectorDocument).toList());
		log.info("已导入 {} 条演示知识库资料", documents.size());
	}

	private List<KnowledgeBaseSeedDocument> readDocuments() throws IOException {
		ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readValue(inputStream, new TypeReference<>() {
			});
		}
	}

	private Document toVectorDocument(KnowledgeBaseSeedDocument document) {
		return new Document(document.content(), Map.of(
				"documentType", "knowledge",
				"knowledgeId", document.id(),
				"title", document.title(),
				"category", document.category(),
				"source", document.source()));
	}
}
