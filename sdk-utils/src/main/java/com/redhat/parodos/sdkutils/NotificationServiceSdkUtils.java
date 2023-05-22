package com.redhat.parodos.sdkutils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Strings;
import com.redhat.parodos.notification.sdk.api.ApiCallback;
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;
import com.redhat.parodos.notification.sdk.model.PageNotificationRecordResponseDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.Data;
import lombok.NonNull;
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
	public static ApiClient getParodosAPiClient()
			throws ApiException, MissingRequiredPropertiesException, InterruptedException {
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
	 * com.redhat.parodos.notification.sdk.api.NotificationRecordApi#getNotificationsAsync(Pageable,
	 * String, String, ApiCallback<PageNotificationRecordResponseDTO>) and retries for 60
	 * seconds.
	 * @param apiclient the API Client
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 * @throws ApiException If the API method invocation fails
	 */
	public static void waitNotificationStart(ApiClient apiclient) throws ApiException, InterruptedException {
		NotificationRecordApi notificationRecordApi = new NotificationRecordApi(apiclient);
		waitAsyncResponse(new FuncExecutor<PageNotificationRecordResponseDTO>() {
			@Override
			public boolean check(PageNotificationRecordResponseDTO result, int statusCode) {
				return statusCode != 200;
			}

			@Override
			public void execute(@NonNull ApiCallback<PageNotificationRecordResponseDTO> callback) throws ApiException {
				notificationRecordApi.getNotificationsAsync(0, 10, null, null, null, callback);
			}
		});
	}

	/**
	 * Executes a @see FuncExecutor. Waits at most 60 seconds for a successful result of
	 * an async API invocation.
	 * @param f the @see FuncExecutor
	 * @param <T> the type of the function executor
	 * @return @see AsyncResult
	 * @throws ApiException if the api invocation fails
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 */
	public static <T> T waitAsyncResponse(FuncExecutor<T> f) throws ApiException, InterruptedException {
		AsyncResult<T> asyncResult = new AsyncResult<>();
		Lock lock = new ReentrantLock();
		Condition response = lock.newCondition();
		ApiCallback<T> apiCallback = new ApiCallback<T>() {

			@Override
			public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
				log.info("onFailure {}", e.getMessage());
				try {
					f.execute(this);
				}
				catch (ApiException apie) {
					asyncResult.setError(apie.getMessage());
					signal();
				}
			}

			@Override
			public void onSuccess(T result, int statusCode, Map<String, List<String>> responseHeaders) {
				if (f.check(result, statusCode)) {
					try {
						f.execute(this);
					}
					catch (ApiException apie) {
						asyncResult.setError(apie.getMessage());
						signal();
					}
				}
				else {
					asyncResult.setStatusCode(statusCode);
					asyncResult.setResult(result);
					asyncResult.setError(null);
					signal();
				}
			}

			@Override
			public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
			}

			@Override
			public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
			}

			private void signal() {
				lock.lock();
				try {
					response.signal();
				}
				finally {
					lock.unlock();
				}
			}
		};
		f.execute(apiCallback);
		lock.lock();
		try {
			// should be more than enough
			response.await(60, TimeUnit.SECONDS);
			if (asyncResult.getError() != null) {
				throw new ApiException(
						"An error occurred while executing waitAsyncResponse: " + asyncResult.getError());
			}
		}
		finally {
			lock.unlock();
		}
		return asyncResult.getResult();
	}

	public interface FuncExecutor<T> {

		/**
		 * Defines the @see ApiCallback to execute
		 * @param callback the
		 * @throws ApiException If the API callback invocation fails
		 */
		void execute(@NonNull ApiCallback<T> callback) throws ApiException;

		/**
		 * Define when considering an ApiCallback result as successful.
		 * @param result the result to check
		 * @return {true} if it is necessary to continue monitoring the result, {false}
		 * when it's possible to stop the monitoring.
		 */
		default boolean check(T result, int statusCode) {
			return true;
		}

	}

	@Data
	private static class AsyncResult<T> {

		private String error;

		T result;

		int statusCode;

	}

}
