package com.project.demo.service.ai.ask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 自助问答意图识别器。
 *
 * <p>在知识库检索和回答生成前识别用户问题是否属于客服业务范围。</p>
 */
@Component
@ConditionalOnProperty(name = "app.ai.ask.provider", havingValue = "deepseek")
public class DeepSeekAskIntentClassifier implements AskIntentClassifier {

	private static final Logger log = LoggerFactory.getLogger(DeepSeekAskIntentClassifier.class);

	private final ChatClient chatClient;

	public DeepSeekAskIntentClassifier(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@Override
	public AskIntent classify(String question) {
		try {
			IntentResponse response = chatClient.prompt()
					.system("""
							你是企业客服系统的意图识别器。判断用户问题是否与客服业务相关。
							业务相关包括：账号、登录、密码、系统使用、工单、账单、扣费、退款、投诉、故障、订单、服务支持、转人工。
							业务无关包括：天气、新闻、闲聊、写作、诗歌、股票、星座、通用百科、与本企业客服无关的生活问题。
							只返回 JSON：{"intent":"BUSINESS_RELATED|UNRELATED"}。
							不解释，不回答用户问题。
							""")
					.user("用户问题：" + question)
					.call()
					.entity(IntentResponse.class);
			if (response == null || response.intent() == null) {
				log.warn("AI意图识别无有效响应，默认进入业务问答链路");
				return AskIntent.BUSINESS_RELATED;
			}
			if ("UNRELATED".equalsIgnoreCase(response.intent())) {
				return AskIntent.UNRELATED;
			}
			return AskIntent.BUSINESS_RELATED;
		}
		catch (RuntimeException exception) {
			log.warn("AI意图识别失败，默认进入业务问答链路: {}", exception.getMessage());
			return AskIntent.BUSINESS_RELATED;
		}
	}

	private record IntentResponse(String intent) {
	}
}
