package com.redhat.parodos.integration.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.integration.notification.common.NotificationTestBuilder;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.sdk.model.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.sdk.model.PageNotificationRecordResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import org.springframework.http.HttpHeaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class NotificationRecordTest {

	String BASE_PATH;

	private static final String user = "test";

	private static final String password = "test";

	private NotificationRecordApi recordApiInstance;

	private NotificationMessageApi messageApiInstance;

	private int listNotificationsExpectedCount = 0;

	private int countNotificationsExpectedCount = 0;

	@Before
	public void setUp() throws ApiException, InterruptedException {

		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		log.info("CHECK NotificationWorkFlowTask basePath: {}", components.apiClient().getBasePath());
		recordApiInstance = new NotificationRecordApi(components.apiClient());
		messageApiInstance = new NotificationMessageApi(components.apiClient());
	}

	@Test
	public void notificationServiceHappyFlow() {
		String testName = "notificationServiceHappyHappyFlow";

		assertDoesNotThrow(() -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", List.of(user));
			createNotificationMessage(testName, "type1", "body1", "subject1", List.of(user));
			createNotificationMessage(testName, "type2", "body2", "subject2", List.of(user));
			createNotificationMessage(testName, "type3", "body3", "subject3", List.of("test2"));

			countNotificationRecord(testName);
			PageNotificationRecordResponseDTO notificationsAfterCreation = listNotificationRecord(testName, null);
			assertNotNull(notificationsAfterCreation.getContent());
			NotificationRecordResponseDTO notificationRecord1 = notificationsAfterCreation.getContent().get(0);
			logTestStep(testName, "Update one Notification Record as \"READ\"");
			updateNotificationRecord(testName, notificationRecord1.getId(), "READ");

			countNotificationRecord(testName);
			PageNotificationRecordResponseDTO notificationsAfterUpdate = listNotificationRecord(testName, null);
			assertNotNull(notificationsAfterUpdate.getContent());
			NotificationRecordResponseDTO notificationRecord2 = notificationsAfterUpdate.getContent().get(1);
			deleteNotificationRecord(testName, notificationRecord2.getId());

			countNotificationRecord(testName);
			listNotificationRecord(testName, null);
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
	public void notificationServiceListInvalidSortErr() {
		String testName = "notificationServiceListInvalidSortErr";

		ApiException e = assertThrows(ApiException.class, () -> {
			listNotificationRecord(testName, List.of(user));
		});
		assertEquals(400, e.getCode());
	}

	@Test
	public void notificationServiceListAllowedSort() {
		String testName = "notificationServiceListAllowedSort";

		assertDoesNotThrow(() -> {
			listNotificationRecord(testName,
					Arrays.asList("id", "notificationMessage.subject", "notificationMessage.fromuser",
							"notificationMessage.createdOn", "notificationMessage.messageType"));
		});
	}

	@Test
	public void notificationServiceCreateEmptyUsernames() {
		String testName = "notificationServiceCreateMissingUsernames";

		ApiException e = assertThrows(ApiException.class, () -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", new ArrayList<>());
		});
		assertEquals(400, e.getCode());
	}

	@Test
	public void notificationServiceCreateAuthErr() {
		String testName = "notificationServiceCreateAuthErr";

		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		components.apiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		this.recordApiInstance = new NotificationRecordApi(components.apiClient());

		ApiException e = assertThrows(ApiException.class, () -> {
			createNotificationMessage(testName, "type0", "body0", "subject0", List.of(user));
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceGetAuthErr() {
		String testName = "notificationServiceGetAuthErr";
		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		components.apiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		this.recordApiInstance = new NotificationRecordApi(components.apiClient());

		ApiException e = assertThrows(ApiException.class, () -> {
			listNotificationRecord(testName, null);
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceCountAuthErr() {
		String testName = "notificationServiceCountAuthErr";

		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		components.apiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		this.recordApiInstance = new NotificationRecordApi(components.apiClient());
		ApiException e = assertThrows(ApiException.class, () -> {
			countNotificationRecord(testName);
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceDeleteAuthErr() {
		String testName = "notificationServiceDeleteAuthErr";
		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		components.apiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		this.recordApiInstance = new NotificationRecordApi(components.apiClient());

		ApiException e = assertThrows(ApiException.class, () -> {
			deleteNotificationRecord(testName, UUID.randomUUID());
		});
		assertEquals(401, e.getCode());
	}

	@Test
	public void notificationServiceUpdateAuthErr() {
		String testName = "notificationServiceUpdateAuthErr";
		NotificationTestBuilder.TestComponents components = new NotificationTestBuilder().build();
		components.apiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "");
		this.recordApiInstance = new NotificationRecordApi(components.apiClient());

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

		if (usernames.contains(user)) {
			countNotificationsExpectedCount++;
			listNotificationsExpectedCount++;
		}
	}

	private void logTestStep(String testName, String stepDescription) {
		log.info("############## " + testName + ": " + stepDescription);
	}

	private void countNotificationRecord(String testName) throws ApiException {
		logTestStep(testName, "Count Notification Records for the user");
		Integer count = this.recordApiInstance.countUnreadNotifications("UNREAD");
		log.info("Found {} notification records for the user", count);
		assertEquals(countNotificationsExpectedCount, count.intValue());
	}

	private PageNotificationRecordResponseDTO listNotificationRecord(String testName, List<String> sort)
			throws ApiException {
		logTestStep(testName, "List Notification Records for the user");
		PageNotificationRecordResponseDTO result = this.recordApiInstance.getNotifications(0, 10, sort, null, null);
		List<NotificationRecordResponseDTO> content = result.getContent();
		assertNotNull(content);
		log.info(content.toString());
		assertEquals(listNotificationsExpectedCount, content.size());
		return result;
	}

	private void updateNotificationRecord(String testName, UUID notificationRecordId, String status)
			throws ApiException {

		logTestStep(testName, "Update one Notification Record as \"" + status + "\"");
		this.recordApiInstance.updateNotificationStatusById(notificationRecordId, status);
		countNotificationsExpectedCount--;
	}

	private void deleteNotificationRecord(String testName, UUID notificationRecordId) throws ApiException {
		logTestStep(testName, "Delete Notification Record");
		this.recordApiInstance.deleteNotification(notificationRecordId);
		countNotificationsExpectedCount--;
		listNotificationsExpectedCount--;
	}

}
