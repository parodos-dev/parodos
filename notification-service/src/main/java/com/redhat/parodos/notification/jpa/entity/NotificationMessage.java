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
package com.redhat.parodos.notification.jpa.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * The message associated with a notification. Want to ensure that the potentially large
 * message is not duplicated for each notification, as such, it is stored separately.
 * There is some work to be done to ensure this is happening efficiently from the DB
 * perspective.
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Entity
@Setter
@Getter
@Table(name = "NotificationMessage")
public class NotificationMessage extends AbstractEntity {

	/**
	 * Users that the message will be delivered to
	 */
	@ElementCollection
	private List<String> usernames;

	/**
	 * Groups that the message will be delivered to
	 */
	@ElementCollection
	private List<String> groupNames;

	@OneToOne(mappedBy = "notificationMessage")
	private NotificationRecord notificationRecord;

	@Column(columnDefinition = "text")
	private String subject;

	@Column(name = "created_on")
	private Instant createdOn;

	/**
	 * maybe useful for selecting messages without looking at the body. Can make an enum
	 */
	@Column(name = "message_type")
	private String messageType;

	@NotNull
	@Column(columnDefinition = "text")
	private String body;

	private String fromuser;

	@Override
	public String toString() {
		return "NotificationMessage{" + "usernames=" + usernames + ", groupNames=" + groupNames
				+ ", notificationRecord=" + notificationRecord + ", subject='" + subject + '\'' + ", createdOn="
				+ createdOn + ", messageType='" + messageType + '\'' + ", body='" + body + '\'' + ", from='" + fromuser
				+ '\'' + '}';
	}

}
