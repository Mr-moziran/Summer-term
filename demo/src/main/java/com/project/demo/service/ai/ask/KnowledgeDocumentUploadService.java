package com.project.demo.service.ai.ask;

import com.project.demo.domain.dto.response.KnowledgeDocumentUploadResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员知识库文档上传服务。
 *
 * <p>当前支持 txt/md 文本文档，上传后按固定窗口切片并写入 Spring AI VectorStore。</p>
 */
@Service
public class KnowledgeDocumentUploadService {

	private static final Logger log = LoggerFactory.getLogger(KnowledgeDocumentUploadService.class);

	private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

	private static final int CHUNK_SIZE = 800;

	private static final int CHUNK_OVERLAP = 100;

	private final VectorStore vectorStore;

	public KnowledgeDocumentUploadService(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	public KnowledgeDocumentUploadResponse upload(MultipartFile file, String title) {
		validateFile(file);
		String filename = normalizeFilename(file.getOriginalFilename());
		String documentTitle = normalizeTitle(title, filename);
		String text = readText(file);
		List<String> chunks = splitText(text);
		if (chunks.isEmpty()) {
			throw new IllegalArgumentException("文档内容不能为空");
		}

		vectorStore.add(toDocuments(chunks, documentTitle, filename));
		log.info("知识库文档已上传: title={}, filename={}, chunkCount={}",
				documentTitle, filename, chunks.size());
		return new KnowledgeDocumentUploadResponse(documentTitle, filename, chunks.size());
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("上传文件不能为空");
		}
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new IllegalArgumentException("上传文件不能超过 5MB");
		}
		String filename = normalizeFilename(file.getOriginalFilename());
		String lowerCaseFilename = filename.toLowerCase(Locale.ROOT);
		if (!lowerCaseFilename.endsWith(".txt") && !lowerCaseFilename.endsWith(".md")) {
			throw new IllegalArgumentException("仅支持 .txt 或 .md 文档");
		}
	}

	private String readText(MultipartFile file) {
		try {
			return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
		}
		catch (IOException exception) {
			throw new IllegalArgumentException("读取上传文件失败");
		}
	}

	private List<String> splitText(String text) {
		List<String> chunks = new ArrayList<>();
		int start = 0;
		while (start < text.length()) {
			int end = Math.min(start + CHUNK_SIZE, text.length());
			String chunk = text.substring(start, end).trim();
			if (!chunk.isBlank()) {
				chunks.add(chunk);
			}
			if (end == text.length()) {
				break;
			}
			start = Math.max(0, end - CHUNK_OVERLAP);
		}
		return chunks;
	}

	private List<Document> toDocuments(List<String> chunks, String title, String filename) {
		List<Document> documents = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			documents.add(new Document(chunks.get(i), Map.of(
					"type", "knowledge",
					"title", title,
					"filename", filename,
					"chunkIndex", i)));
		}
		return documents;
	}

	private String normalizeFilename(String filename) {
		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("上传文件名不能为空");
		}
		String normalized = filename.replace("\\", "/");
		int slashIndex = normalized.lastIndexOf('/');
		if (slashIndex >= 0) {
			normalized = normalized.substring(slashIndex + 1);
		}
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("上传文件名不能为空");
		}
		return normalized;
	}

	private String normalizeTitle(String title, String filename) {
		if (title == null || title.isBlank()) {
			int dotIndex = filename.lastIndexOf('.');
			return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
		}
		return title.trim();
	}
}
