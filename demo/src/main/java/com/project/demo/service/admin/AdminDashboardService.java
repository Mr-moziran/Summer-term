package com.project.demo.service.admin;

import com.project.demo.domain.dto.response.AdminDashboardResponse;
import com.project.demo.domain.dto.response.AdminModuleResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员后台首页聚合服务。
 *
 * <p>把数据大盘摘要和管理员可操作模块入口聚合为一个面板响应，便于前端登录后直接渲染管理后台。</p>
 */
@Service
public class AdminDashboardService {

	private final AdminStatsService adminStatsService;

	public AdminDashboardService(AdminStatsService adminStatsService) {
		this.adminStatsService = adminStatsService;
	}

	@Transactional(readOnly = true)
	public AdminDashboardResponse getDashboard() {
		return new AdminDashboardResponse(adminStatsService.getStats(), modules());
	}

	private List<AdminModuleResponse> modules() {
		return List.of(
				new AdminModuleResponse("tickets", "工单管理", "/admin/tickets", "查看全部工单并分配客服处理"),
				new AdminModuleResponse("users", "用户管理", "/admin/users", "查看用户列表并启用或禁用账号"),
				new AdminModuleResponse("agents", "客服绩效", "/admin/agents", "查看客服处理量、解决量、回复量和响应时间"),
				new AdminModuleResponse("knowledge", "知识库管理", "/admin/knowledge", "上传知识库文档供 AI 自助问答检索"));
	}
}
