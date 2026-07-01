package com.project.demo.service.ai;

import com.project.demo.entity.Ticket;

/**
 * 已解决工单索引端口。
 *
 * <p>当工单进入 RESOLVED 状态后，将解决方案写入可检索知识库；默认实现为空操作。</p>
 */
public interface ResolvedTicketIndex {

	void index(Ticket ticket, String solution);
}
