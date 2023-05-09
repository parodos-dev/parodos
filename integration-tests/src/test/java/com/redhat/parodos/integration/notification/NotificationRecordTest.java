package com.redhat.parodos.integration.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class NotificationRecordTest {

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

		assertDoesNotThrow(() -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", List.of("test"));
			createNotificationMessage(testName, "type1", "body1", "subject1", List.of("test"));
			createNotificationMessage(testName, "type2", "body2", "subject2", List.of("test"));
			createNotificationMessage(testName, "type3", "body3", "subject3", List.of("test2"));

			countNotificationRecord(testName, 3);
			PageNotificationRecordResponseDTO notificationsAfterCreation = listNotificationRecord(testName, 3);
			NotificationRecordResponseDTO notificationRecord1 = notificationsAfterCreation.getContent().get(0);
			logTestStep(testName, "Update one Notification Record as \"READ\"");
			updateNotificationRecord(testName, notificationRecord1.getId(), "READ");

			countNotificationRecord(testName, 2);
			PageNotificationRecordResponseDTO notificationsAfterUpdate = listNotificationRecord(testName, 3);
			NotificationRecordResponseDTO notificationRecord2 = notificationsAfterUpdate.getContent().get(1);
			deleteNotificationRecord(testName, notificationRecord2.getId());

			countNotificationRecord(testName, 1);
			listNotificationRecord(testName, 2);
		});
	}

	@Test
	public void notificationServiceCreateMissingUsernames() {
		String testName = "notificationServiceCreateMissingUsernames";

		ApiException e = assertThrows(ApiException.class, () -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", null);
		});
		assertEquals(400, e.getCode());
	}

	@Test
	public void notificationServiceCreateEmptyUsernames() {
		String testName = "notificationServiceCreateMissingUsernames";

		ApiException e = assertThrows(ApiException.class, () -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", new ArrayList());
		});
		assertEquals(400, e.getCode());
	}

	@Test
	public void notificationServiceCreateAuthErr() {
		String testName = "notificationServiceCreateAuthErr";

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		apiClient.setBasePath(BASE_PATH);
		this.messageApiInstance = new NotificationMessageApi(apiClient);

		ApiException e = assertThrows(ApiException.class, () -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", List.of("test"));
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceGetAuthErr() {
		String testName = "notificationServiceGetAuthErr";

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		apiClient.setBasePath(BASE_PATH);
		this.recordApiInstance = new NotificationRecordApi(apiClient);

		ApiException e = assertThrows(ApiException.class, () -> {
			listNotificationRecord(testName, 0);
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceCountAuthErr() {
		String testName = "notificationServiceCountAuthErr";

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		apiClient.setBasePath(BASE_PATH);
		this.recordApiInstance = new NotificationRecordApi(apiClient);

		ApiException e = assertThrows(ApiException.class, () -> {
			countNotificationRecord(testName, 0);
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceDeleteAuthErr() {
		String testName = "notificationServiceDeleteAuthErr";

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		apiClient.setBasePath(BASE_PATH);
		this.recordApiInstance = new NotificationRecordApi(apiClient);

		ApiException e = assertThrows(ApiException.class, () -> {
			deleteNotificationRecord(testName, UUID.randomUUID());
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceUpdateAuthErr() {
		String testName = "notificationServiceUpdateAuthErr";

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		apiClient.setBasePath(BASE_PATH);
		this.recordApiInstance = new NotificationRecordApi(apiClient);

		ApiException e = assertThrows(ApiException.class, () -> {
			updateNotificationRecord(testName, UUID.randomUUID(), "READ");
		});
		assertEquals(401, e.getCode());
	}

	// Helper functions

	private void createNotificationMessage(String testName, String messageType, String body, String subject,
			List<String> usernames) throws ApiException {

		logTestStep(testName, "Create the notification messages");
		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();

		notificationMessageCreateRequestDTO.messageType(messageType);
		notificationMessageCreateRequestDTO.body(body);
		notificationMessageCreateRequestDTO.subject(subject);
		notificationMessageCreateRequestDTO.usernames(usernames);

		this.messageApiInstance.create(notificationMessageCreateRequestDTO);
	}

	private void logTestStep(String testName, String stepDescription) {
		log.info("############## " + testName + ": " + stepDescription);
	}

	private void countNotificationRecord(String testName, int notificationRecordsExpectedCount) throws ApiException {
		logTestStep(testName, "Count Notification Records for the user");
		Integer count = this.recordApiInstance.countUnreadNotifications("UNREAD");
		log.info("Found ", count, "notification records for the user");
		assertEquals(notificationRecordsExpectedCount, count.intValue());
	}

	private PageNotificationRecordResponseDTO listNotificationRecord(String testName,
			int notificationRecordsExpectedCount) throws ApiException {
		logTestStep(testName, "List Notification Records for the user");
		PageNotificationRecordResponseDTO result = this.recordApiInstance.getNotifications(new Pageable(), null, null);
		List<NotificationRecordResponseDTO> content = result.getContent();
		log.info(content.toString());
		assertEquals(notificationRecordsExpectedCount, content.size());
		return result;
	}

	private void updateNotificationRecord(String testName, UUID notificationRecordId, String status)
			throws ApiException {
		logTestStep(testName, "Update Notification Records for the user");
		this.recordApiInstance.updateNotificationStatusById(notificationRecordId, status);
	}

	private void deleteNotificationRecord(String testName, UUID notificationRecordId) throws ApiException {
		logTestStep(testName, "Delete Notification Record");
		this.recordApiInstance.deleteNotification(notificationRecordId);
	}

}
