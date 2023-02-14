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

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

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
	private List<String> groupnames;

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

	public List<String> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}

	public List<String> getGroupnames() {
		return groupnames;
	}

	public void setGroupnames(List<String> groupnames) {
		this.groupnames = groupnames;
	}

	public NotificationRecord getNotificationsRecord() {
		return notificationRecord;
	}

	public void setNotificationsRecord(NotificationRecord notificationRecord) {
		this.notificationRecord = notificationRecord;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Instant getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Instant createdOn) {
		this.createdOn = createdOn;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFromuser() {
		return fromuser;
	}

	public void setFromuser(String fromuser) {
		this.fromuser = fromuser;
	}

	@Override
	public String toString() {
		return "NotificationMessage{" + "usernames=" + usernames + ", groupnames=" + groupnames
				+ ", notificationRecord=" + notificationRecord + ", subject='" + subject + '\'' + ", createdOn="
				+ createdOn + ", messageType='" + messageType + '\'' + ", body='" + body + '\'' + ", from='" + fromuser
				+ '\'' + '}';
	}

}
