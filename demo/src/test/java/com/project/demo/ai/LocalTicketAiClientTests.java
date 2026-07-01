package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.demo.ticket.Ticket;
import com.project.demo.ticket.TicketCategory;
import com.project.demo.ticket.TicketPriority;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import org.junit.jupiter.api.Test;

class LocalTicketAiClientTests {

	private final LocalTicketAiClient client = new LocalTicketAiClient();

	@Test
	void classifiesLoginIssueAsTechnicalHighPriority() {
		TicketClassification classification = client.classify(ticket("无法登录系统", "输入正确密码后仍提示密码错误"));

		assertThat(classification.category()).isEqualTo(TicketCategory.TECHNICAL);
		assertThat(classification.priority()).isEqualTo(TicketPriority.HIGH);
	}

	@Test
	void classifiesBillingIssueAsBillingMediumPriority() {
		TicketClassification classification = client.classify(ticket("账单问题", "扣费金额不正确，希望退款"));

		assertThat(classification.category()).isEqualTo(TicketCategory.BILLING);
		assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIUM);
	}

	@Test
	void generatesDeterministicDraftReply() {
		String draft = client.draftReply(ticket("无法登录系统", "输入正确密码后仍提示密码错误"), java.util.List.of());

		assertThat(draft).contains("无法登录系统");
		assertThat(draft).contains("输入正确密码后仍提示密码错误");
	}

	private Ticket ticket(String title, String description) {
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		return new Ticket(title, description, submitter);
	}
}
