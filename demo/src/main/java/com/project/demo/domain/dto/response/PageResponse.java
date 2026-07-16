package com.project.demo.domain.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 统一分页响应 DTO，包装 Spring Data Page 并输出前端常用分页字段。
 */
public class PageResponse<T> {

	private final List<T> content;
	private final int page;
	private final int size;
	private final long totalElements;
	private final int totalPages;
	private final boolean first;
	private final boolean last;

	private PageResponse(Page<T> page) {
		this.content = page.getContent();
		this.page = page.getNumber();
		this.size = page.getSize();
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.first = page.isFirst();
		this.last = page.isLast();
	}

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(page);
	}

	public List<T> getContent() {
		return content;
	}

	public int getPage() {
		return page;
	}

	public int getSize() {
		return size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public boolean isFirst() {
		return first;
	}

	public boolean isLast() {
		return last;
	}
}
