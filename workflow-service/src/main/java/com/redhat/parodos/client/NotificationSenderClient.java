package com.redhat.parodos.client;

import java.util.Base64;
import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSenderClient implements Notifier {

	private final NotificationMessageApi client;

	public NotificationSenderClient(@Value("${notification.url}") String url,
			@Value("${notification.auth.basic.user}") String user,
			@Value("${notification.auth.basic.password}") String password) {
		ApiClient apiClient = new com.redhat.parodos.notification.sdk.api.ApiClient().setBasePath(url).addDefaultHeader(
				HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes()));
		client = new NotificationMessageApi(apiClient);
	}

	@Override
	public void send(String subject, String body) {
		var m = new NotificationMessageCreateRequestDTO();
		m.setSubject(subject);
		m.setBody(body);
		setUserIfUnset(m);
		send(m);
	}

	@Override
	public void send(NotificationMessageCreateRequestDTO message) {
		setUserIfUnset(message);
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

	private void setUserIfUnset(NotificationMessageCreateRequestDTO message) {
		if (message.getUsernames() == null || message.getUsernames().isEmpty()) {
			message.setUsernames(List.of(SecurityUtils.getUsername()));
		}
	}

}
