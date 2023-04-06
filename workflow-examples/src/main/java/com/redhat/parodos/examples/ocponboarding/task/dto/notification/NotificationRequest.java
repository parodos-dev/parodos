package com.redhat.parodos.examples.ocponboarding.task.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * request DTO of notification service
 *
 * @author Richard Wang (Github: RichardW98)
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest implements Serializable {

	private List<String> usernames;

	private String subject;

	@Builder.Default
	private String messageType = "MIGRATE";

	private String body;

}
