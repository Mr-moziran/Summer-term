package com.project.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 用户自助问答配置。
 */
@Component
public class AskAiProperties {

	private final double highThreshold;

	private final double mediumThreshold;

	private final int topK;

	public AskAiProperties(
			@Value("${app.ai.ask.high-threshold:0.82}") double highThreshold,
			@Value("${app.ai.ask.medium-threshold:0.70}") double mediumThreshold,
			@Value("${app.ai.ask.top-k:3}") int topK) {
		this.highThreshold = highThreshold;
		this.mediumThreshold = mediumThreshold;
		this.topK = topK;
	}

	public double getHighThreshold() {
		return highThreshold;
	}

	public double getMediumThreshold() {
		return mediumThreshold;
	}

	public int getTopK() {
		return topK;
	}
}
