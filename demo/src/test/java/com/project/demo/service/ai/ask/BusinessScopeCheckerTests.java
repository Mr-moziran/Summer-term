package com.project.demo.service.ai.ask;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 业务范围判断测试。
 */
class BusinessScopeCheckerTests {

	private final BusinessScopeChecker checker = new BusinessScopeChecker();

	@Test
	void shouldRejectClearlyUnrelatedWeatherQuestion() {
		assertTrue(checker.isOutOfScope("今天天气怎么样？"));
	}

	@Test
	void shouldKeepBusinessQuestionInScope() {
		assertFalse(checker.isOutOfScope("退款申请后多久到账？"));
	}

	@Test
	void shouldKeepQuestionThatMentionsBusinessContextInScope() {
		assertFalse(checker.isOutOfScope("天气导致无法访问系统怎么办？"));
	}
}
