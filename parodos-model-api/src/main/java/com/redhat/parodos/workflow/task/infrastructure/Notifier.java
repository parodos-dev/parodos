package com.redhat.parodos.workflow.task.infrastructure;

import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;

public interface Notifier {

	void send(NotificationMessageCreateRequestDTO message);

	void trySend(NotificationMessageCreateRequestDTO message) throws ApiException;

}
