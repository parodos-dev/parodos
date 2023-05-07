package com.redhat.parodos.notification.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationRecordResponseDTOTest {

	@Test
	void toModel() {
		NotificationRecord entity = Mockito.mock(NotificationRecord.class);
		NotificationMessage notificationMessage = Mockito.mock(NotificationMessage.class);

		String messageBody = "message-body-test";
		Instant messageCreation = Instant.now();
		String folderName = "test-folder";
		String messageSender = "message-sender-test";
		Boolean isRead = false;
		String messageSubject = "message-subject-test";
		String messageType = "message-type-test";
		List<String> tags = Collections.singletonList("tag-test");
		UUID uuid = UUID.randomUUID();

		Mockito.when(entity.getNotificationMessage()).thenReturn(notificationMessage);
		Mockito.when(entity.getFolder()).thenReturn(folderName);
		Mockito.when(entity.isRead()).thenReturn(isRead);
		Mockito.when(entity.getTags()).thenReturn(tags);
		Mockito.when(entity.getId()).thenReturn(uuid);

		Mockito.when(notificationMessage.getBody()).thenReturn(messageBody);
		Mockito.when(notificationMessage.getCreatedOn()).thenReturn(messageCreation);
		Mockito.when(notificationMessage.getFromuser()).thenReturn(messageSender);
		Mockito.when(notificationMessage.getSubject()).thenReturn(messageSubject);
		Mockito.when(notificationMessage.getMessageType()).thenReturn(messageType);

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