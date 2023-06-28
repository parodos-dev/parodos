package com.redhat.parodos.sdkutils;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.MissingRequiredPropertiesException;

/***
 * A utility class to ease the writing of new examples.
 */

@Slf4j
public abstract class NotificationServiceUtils {

	private NotificationServiceUtils() {
	}

	/**
	 * Creates and configures the APIClient using the configuration properties available
	 * in environment variables.
	 * @return the ApiClient
	 */
	public static ApiClient getParodosAPiClient() throws MissingRequiredPropertiesException {
		ApiClient apiClient = Configuration.getDefaultApiClient();
		String serverIp = Optional.ofNullable(System.getenv("NOTIFICATION_SERVICE_HOST")).orElse("localhost");
		String serverPort = Optional.ofNullable(System.getenv("SERVER_PORT")).orElse("8081");

		if (Strings.isNullOrEmpty(serverIp) || Strings.isNullOrEmpty(serverPort)) {
			throw new IllegalArgumentException("NOTIFICATION_SERVER_ADDRESS and NOTIFICATION_SERVER_PORT must be set");
		}

		int port = Integer.parseInt(serverPort);
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("serverPort must be > 0 && <= 65535");
		}

		String basePath = "http://%s:%s".formatted(serverIp, serverPort);
		log.info("serverIp is: {}, serverPort is {}. Set BasePath to {}", serverIp, serverPort, basePath);

		apiClient.setBasePath(basePath);
		apiClient.addDefaultHeader("Authorization", "Basic " + CredUtils.getBase64Creds("test", "test"));
		waitNotificationStart(apiClient);
		return apiClient;
	}

	/**
	 * Invokes @see
	 * com.redhat.parodos.notification.sdk.api.NotificationRecordApi#getNotifications(Pageable,
	 * String, String) and retries for 120 seconds.
	 * @param apiclient the API Client
	 */
	public static void waitNotificationStart(ApiClient apiclient) {
		NotificationRecordApi notificationRecordApi = new NotificationRecordApi(apiclient);
		try (var executorService = new RetryExecutorService<Void>()) {
			Callable<Void> task = () -> {
				notificationRecordApi.getNotifications(0, 10, null, null, null);
				return null;
			};

			executorService.submitWithRetry(task);
		}
		catch (Exception e) {
			throw new RuntimeException("Notification Record API is not up and running", e);
		}
	}

}
