package com.redhat.parodos;

import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.workflow.task.infrastructure.Notifier;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSender implements Notifier {

	private final NotificationMessageApi client;

	public NotificationSender(@Value("${notification.url}") String url,
			@Value("${notification.auth.basic.user}") String user,
			@Value("${notification.auth.basic.password}") String password) {
		ApiClient apiClient = new com.redhat.parodos.notification.sdk.api.ApiClient().setBasePath(url).addDefaultHeader(
				HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes()));
		client = new NotificationMessageApi(apiClient);
	}

	@Override
	public void send(NotificationMessageCreateRequestDTO message) {
		try {
			trySend(message);
		}
		catch (ApiException e) {
			log.error("failed sending notification message due to: " + e);
		}
	}

	@Override
	public void trySend(NotificationMessageCreateRequestDTO message) throws ApiException {
		client.create(message);
	}

}
