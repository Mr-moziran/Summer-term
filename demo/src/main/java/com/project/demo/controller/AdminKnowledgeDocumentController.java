package com.project.demo.controller;

import com.project.demo.domain.dto.response.KnowledgeDocumentUploadResponse;
import com.project.demo.service.ai.ask.KnowledgeDocumentUploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员知识库文档接口控制器。
 *
 * <p>用于上传客服知识库资料，上传后的文本片段会写入向量库供用户自助问答检索。</p>
 */
@RestController
@RequestMapping("/api/admin/knowledge-documents")
public class AdminKnowledgeDocumentController {

	private final KnowledgeDocumentUploadService uploadService;

	public AdminKnowledgeDocumentController(KnowledgeDocumentUploadService uploadService) {
		this.uploadService = uploadService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public KnowledgeDocumentUploadResponse upload(
			@RequestPart("file") MultipartFile file,
			@RequestParam(required = false) String title) {
		return uploadService.upload(file, title);
	}
}
