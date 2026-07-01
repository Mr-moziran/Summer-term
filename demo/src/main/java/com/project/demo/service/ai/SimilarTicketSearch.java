package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;
import java.util.List;

/**
 * 相似历史工单检索端口。
 *
 * <p>默认实现返回空列表；PgVector 实现通过 Spring AI VectorStore 查询 vector_store。</p>
 */
public interface SimilarTicketSearch {

	List<SimilarTicketContext> search(Ticket ticket);
}
