package com.project.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.project.demo.ticket.Ticket;
import com.project.demo.user.User;
import com.project.demo.user.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

class PgVectorResolvedTicketIndexTests {

	@Test
	void writesResolvedTicketDocumentWithMetadata() {
		VectorStore vectorStore = org.mockito.Mockito.mock(VectorStore.class);
		PgVectorResolvedTicketIndex index = new PgVectorResolvedTicketIndex(vectorStore);
		Ticket ticket = ticket("无法登录系统", "输入正确密码后仍提示密码错误", 101L);

		index.index(ticket, "清除Cookie后重试");

		ArgumentCaptor<List<Document>> documentsCaptor = ArgumentCaptor.forClass(List.class);
		verify(vectorStore).add(documentsCaptor.capture());
		Document document = documentsCaptor.getValue().getFirst();
		assertThat(document.getText()).contains("无法登录系统");
		assertThat(document.getText()).contains("输入正确密码后仍提示密码错误");
		assertThat(document.getText()).contains("清除Cookie后重试");
		assertThat(document.getMetadata()).containsEntry("ticketId", 101L);
		assertThat(document.getMetadata()).containsEntry("title", "无法登录系统");
		assertThat(document.getMetadata()).containsEntry("solution", "清除Cookie后重试");
	}

	private Ticket ticket(String title, String description, Long id) {
		User submitter = new User("submitter", "submitter@example.com", "{bcrypt}password", UserRole.USER);
		Ticket ticket = new Ticket(title, description, submitter);
		try {
			java.lang.reflect.Field field = Ticket.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(ticket, id);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
		return ticket;
	}
}
