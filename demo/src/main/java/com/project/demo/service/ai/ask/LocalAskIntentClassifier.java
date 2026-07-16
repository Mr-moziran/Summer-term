package com.project.demo.service.ai.ask;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地自助问答意图识别器。
 *
 * <p>只拦截明显与客服业务无关的问题；无法确定时默认放行业务链路，避免误伤真实用户问题。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.provider", havingValue = "local", matchIfMissing = true)
public class LocalAskIntentClassifier implements AskIntentClassifier {

	@Override
	public AskIntent classify(String question) {
		String normalizedQuestion = question == null ? "" : question.trim();
		if (isObviouslyUnrelated(normalizedQuestion)) {
			return AskIntent.UNRELATED;
		}
		return AskIntent.BUSINESS_RELATED;
	}

	private boolean isObviouslyUnrelated(String question) {
		return question.contains("天气")
				|| question.contains("几点")
				|| question.contains("日期")
				|| question.contains("今天星期")
				|| question.contains("讲个笑话")
				|| question.contains("写首诗")
				|| question.contains("新闻")
				|| question.contains("股票")
				|| question.contains("星座");
	}
}
