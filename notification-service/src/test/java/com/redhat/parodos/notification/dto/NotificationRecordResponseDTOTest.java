package com.redhat.parodos.notification.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationRecordResponseDTOTest {

	@Test
	void toModel() {
		NotificationRecord entity = mock(NotificationRecord.class);
		NotificationMessage notificationMessage = mock(NotificationMessage.class);

		String messageBody = "message-body-test";
		Instant messageCreation = Instant.now();
		String folderName = "test-folder";
		String messageSender = "message-sender-test";
		Boolean isRead = false;
		String messageSubject = "message-subject-test";
		String messageType = "message-type-test";
		List<String> tags = Collections.singletonList("tag-test");
		UUID uuid = UUID.randomUUID();

		when(entity.getNotificationMessage()).thenReturn(notificationMessage);
		when(entity.getFolder()).thenReturn(folderName);
		when(entity.isRead()).thenReturn(isRead);
		when(entity.getTags()).thenReturn(tags);
		when(entity.getId()).thenReturn(uuid);

		when(notificationMessage.getBody()).thenReturn(messageBody);
		when(notificationMessage.getCreatedOn()).thenReturn(messageCreation);
		when(notificationMessage.getFromuser()).thenReturn(messageSender);
		when(notificationMessage.getSubject()).thenReturn(messageSubject);
		when(notificationMessage.getMessageType()).thenReturn(messageType);

		NotificationRecordResponseDTO result = NotificationRecordResponseDTO.toModel(entity);

		assertEquals(messageBody, result.getBody());
		assertEquals(messageCreation, result.getCreatedOn());
		assertEquals(folderName, result.getFolder());
		assertEquals(messageSender, result.getFromuser());
		assertEquals(isRead, result.isRead());
		assertEquals(messageSubject, result.getSubject());
		assertEquals(messageType, result.getMessageType());
		assertEquals(tags, result.getTags());
		assertEquals(uuid, result.getId());

	}

}
