/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.notification.dto;

import java.util.List;

import com.redhat.parodos.notification.validation.ValidNotificationMessageUserOrGroup;
import lombok.Data;

/**
 * Notification message create request DTO
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@ValidNotificationMessageUserOrGroup
@Data
public class NotificationMessageCreateRequestDTO {

	private List<String> usernames;

	private List<String> groupNames;

	private String subject;

	private String messageType;

	private String body;

	@Override
	public String toString() {
		return "NotificationMessageCreateRequestDTO{" + "usernames=" + usernames + ", groupNames=" + groupNames
				+ ", subject='" + subject + '\'' + ", messageType='" + messageType + '\'' + ", body='" + body + '\''
				+ '}';
	}

}
