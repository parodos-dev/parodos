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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Notification record response DTO
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Data
@EqualsAndHashCode
public class NotificationRecordResponseDTO {

	private UUID id;

	private String subject;

	private Instant createdOn;

	private String messageType;

	private String body;

	private String fromuser;

	private boolean read;

	private List<String> tags;

	private String folder;

	@Override
	public String toString() {
		return "NotificationDTO {" + "id=" + id + ", subject='" + subject + '\'' + ", createdOn=" + createdOn
				+ ", messageType='" + messageType + '\'' + ", body='" + body + '\'' + ", fromuser='" + fromuser + '\''
				+ ", read=" + read + ", tags=" + tags + ", folder='" + folder + '\'' + '}';
	}

	public static NotificationRecordResponseDTO toModel(NotificationRecord entity) {
		NotificationRecordResponseDTO dto = new NotificationRecordResponseDTO();
		NotificationMessage notificationMessage = entity.getNotificationMessage();

		dto.setBody(notificationMessage.getBody());
		dto.setCreatedOn(notificationMessage.getCreatedOn());
		dto.setFolder(entity.getFolder());
		dto.setFromuser(notificationMessage.getFromuser());
		dto.setRead(entity.isRead());
		dto.setSubject(notificationMessage.getSubject());
		dto.setMessageType(notificationMessage.getMessageType());
		dto.setTags(entity.getTags());
		dto.setId(entity.getId());
		return dto;
	}

}
