package com.project.demo.service.ai.ask;

import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 判断用户问题是否属于企业 SaaS 客服的支持范围。
 *
 * <p>明确的非业务主题会被拒答；其余问题保留给知识库和转人工链路，避免因表达不规范误拒绝真实客户问题。</p>
 */
@Component
public class BusinessScopeChecker {

	private static final Set<String> BUSINESS_TERMS = Set.of(
			"账号", "登录", "密码", "邮箱", "成员", "邀请", "套餐", "订阅", "账单", "扣费",
			"付款", "支付", "退款", "发票", "订单", "系统", "页面", "功能", "导出", "权限",
			"附件", "工单", "客服", "投诉", "反馈", "数据", "看板", "企业空间", "服务",
			"报错", "错误", "超时", "加载", "访问", "卡顿", "通知");

	private static final Set<String> OUT_OF_SCOPE_TERMS = Set.of(
			"天气", "气温", "下雨", "降雨", "温度", "空气质量", "天气预报", "新闻", "股票",
			"股价", "基金", "足球", "比赛", "电影", "电视剧", "菜谱", "食谱", "笑话",
			"星座", "旅游", "路线", "翻译", "写诗");

	/**
	 * 明确命中非业务主题且不包含业务关键词时，视为超出支持范围。
	 */
	public boolean isOutOfScope(String question) {
		return containsAny(question, OUT_OF_SCOPE_TERMS) && !containsAny(question, BUSINESS_TERMS);
	}

	private boolean containsAny(String question, Set<String> terms) {
		return terms.stream().anyMatch(question::contains);
	}
}
