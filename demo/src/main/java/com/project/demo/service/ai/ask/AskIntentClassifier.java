package com.project.demo.service.ai.ask;

/**
 * 用户自助问答意图识别器。
 */
public interface AskIntentClassifier {

	AskIntent classify(String question);
}
