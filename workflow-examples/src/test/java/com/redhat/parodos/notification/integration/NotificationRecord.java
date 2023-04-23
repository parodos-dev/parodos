package com.redhat.parodos.notification.integration;

import com.redhat.parodos.notification.sdk.api.*;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.sdk.model.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.sdk.model.PageNotificationRecordResponseDTO;
import com.redhat.parodos.notification.sdk.model.Pageable;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
public class NotificationRecord {

	private static final String BASE_PATH = "http://localhost:8081";

	private NotificationRecordApi recordApiInstance;

	private NotificationMessageApi messageApiInstance;

	@Before
	public void setUp() throws IOException {
		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));
		apiClient.setBasePath(BASE_PATH);
		recordApiInstance = new NotificationRecordApi(apiClient);
		messageApiInstance = new NotificationMessageApi(apiClient);
	}

	@Test
	public void notificationServiceHappyHappyFlow() {
		String testName = "notificationServiceHappyHappyFlow";
		logTestStep(testName, "Create the notification messages");
		createNotificationMessage("type0", "body0", "subject0", "test");
		createNotificationMessage("type1", "body1", "subject1", "test");
		createNotificationMessage("type2", "body2", "subject2", "test");
		createNotificationMessage("type3", "body3", "subject3", "test2");

		validateNotificationRecordCount(testName, 3);
		assertDoesNotThrow(() -> {
			PageNotificationRecordResponseDTO list = validateNotificationRecordList(testName, 3);
			NotificationRecordResponseDTO notificationRecord = list.getContent().get(0);
			logTestStep(testName, "Update one Notification Record as \"READ\"");
			this.recordApiInstance.updateNotificationStatusById(notificationRecord.getId(), "READ");
		});

		validateNotificationRecordCount(testName, 2);
		assertDoesNotThrow(() -> {
			PageNotificationRecordResponseDTO list = validateNotificationRecordList(testName, 3);
			NotificationRecordResponseDTO notificationRecord = list.getContent().get(1);
			this.recordApiInstance.deleteNotification(notificationRecord.getId());
		});

		validateNotificationRecordCount(testName, 1);
		assertDoesNotThrow(() -> {
			PageNotificationRecordResponseDTO list = validateNotificationRecordList(testName, 2);
		});

	}

	private void validateNotificationRecordCount(String testName, int notificationRecordsExpectedCount) {
		logTestStep(testName, "Count Notification Records for the user");
		assertDoesNotThrow(() -> {
			Integer count = this.recordApiInstance.countUnreadNotifications("UNREAD");
			log.info("Found ", count, "notification records for the user");
			assertEquals(notificationRecordsExpectedCount, count.intValue());
		});
	}

	private PageNotificationRecordResponseDTO validateNotificationRecordList(String testName,
			int notificationRecordsExpectedCount) throws ApiException {
		logTestStep(testName, "List Notification Records for the user");
		PageNotificationRecordResponseDTO result = this.recordApiInstance.getNotifications(new Pageable(), null, null);
		List<NotificationRecordResponseDTO> content = result.getContent();
		log.info(content.toString());
		assertEquals(notificationRecordsExpectedCount, content.size());
		return result;
	}

	private void createNotificationMessage(String messageType, String body, String subject, String username) {
		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();

		notificationMessageCreateRequestDTO.messageType(messageType);
		notificationMessageCreateRequestDTO.body(body);
		notificationMessageCreateRequestDTO.subject(subject);
		if (username != null) {
			notificationMessageCreateRequestDTO.usernames(Arrays.asList(username));
		}

		assertDoesNotThrow(() -> {
			this.messageApiInstance.create(notificationMessageCreateRequestDTO);
		});
	}

	private void logTestStep(String testName, String stepDescription) {
		log.info("############## " + testName + ": " + stepDescription);
	}

}
