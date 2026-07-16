package com.project.demo.domain.dto.response;

/**
 * 管理员知识库文档上传响应。
 */
public class KnowledgeDocumentUploadResponse {

	private final String title;

	private final String filename;

	private final int chunkCount;

	public KnowledgeDocumentUploadResponse(String title, String filename, int chunkCount) {
		this.title = title;
		this.filename = filename;
		this.chunkCount = chunkCount;
	}

	public String getTitle() {
		return title;
	}

	public String getFilename() {
		return filename;
	}

	public int getChunkCount() {
		return chunkCount;
	}
}
