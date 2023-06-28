package com.redhat.parodos.integration.notification.common;

import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;
import com.redhat.parodos.notification.sdk.model.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.sdk.model.PageNotificationRecordResponseDTO;
import com.redhat.parodos.sdkutils.NotificationServiceUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationTestBuilder {

	private ApiClient apiClient;

	public TestComponents build() {
		try {
			setupClient();
			cleanUpNotifications();
		}
		catch (InterruptedException | ApiException e) {
			throw new RuntimeException(e);
		}

		return new TestComponents(apiClient);
	}

	private void cleanUpNotifications() throws ApiException {

		PageNotificationRecordResponseDTO notifications = null;
		NotificationRecordApi notificationRecordApi = new NotificationRecordApi(this.apiClient);
		notifications = notificationRecordApi.getNotifications(0, 50, null, null, null);

		if (notifications.getContent() != null && notifications.getContent().size() > 0) {
			for (NotificationRecordResponseDTO record : notifications.getContent()) {
				notificationRecordApi.deleteNotification(record.getId());
			}
			notifications = notificationRecordApi.getNotifications(0, 50, null, null, null);
			assertThat(notifications.getContent()).isNotNull();
			assertThat(notifications.getContent().size()).isEqualTo(0);
		}
	}

	private void setupClient() throws InterruptedException, ApiException {
		apiClient = NotificationServiceUtils.getParodosAPiClient();
		assertThat(apiClient).isNotNull();
	}

	public record TestComponents(ApiClient apiClient) {
	}

}
