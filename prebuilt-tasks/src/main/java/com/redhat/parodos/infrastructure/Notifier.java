package com.redhat.parodos.infrastructure;

import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;

public interface Notifier {

	/**
	 * Send text type message and use the current authenticated user
	 * @param subject
	 * @param body
	 */
	void send(String subject, String body);

	void send(NotificationMessageCreateRequestDTO message);

	void trySend(NotificationMessageCreateRequestDTO message) throws ApiException;

}
