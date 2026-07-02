package com.project.demo.ai;

/**
 * 自助问答模型决策。
 */
public final class AskAiDecision {

	private final String answer;

	private final EscalationRequest escalationRequest;

	private AskAiDecision(String answer, EscalationRequest escalationRequest) {
		this.answer = answer;
		this.escalationRequest = escalationRequest;
	}

	public static AskAiDecision answer(String answer) {
		return new AskAiDecision(answer, null);
	}

	public static AskAiDecision escalate(EscalationRequest escalationRequest) {
		return new AskAiDecision(null, escalationRequest);
	}

	public boolean requiresEscalation() {
		return escalationRequest != null;
	}

	public String getAnswer() {
		return answer;
	}

	public EscalationRequest getEscalationRequest() {
		return escalationRequest;
	}
}
