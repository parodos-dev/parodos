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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The record that keeps track of notifications for a user, if it read and what
 * tags/folder the associated message for the notification is stored in from the user
 * perspective
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Data
@Entity
@Table(name = "NotificationRecord")
@EqualsAndHashCode(callSuper = false)
public class NotificationRecord extends AbstractEntity {

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "notificationmessage_id")
	private NotificationMessage notificationMessage;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_notificationrecord", joinColumns = @JoinColumn(name = "notificationrecord_id"),
			inverseJoinColumns = @JoinColumn(name = "notificationuser_id"))
	private List<NotificationUser> notificationUserList = new ArrayList<>();

	@Column(name = "read")
	private boolean read;

	@ElementCollection
	private List<String> tags;

	@Column(name = "folder")
	private String folder;

	@Override
	public String toString() {
		return "NotificationRecord {" + "notificationMessage=" + notificationMessage + ", read=" + read + ", tags="
				+ tags + ", folder='" + folder + '\'' + ", notificationUserList=" + notificationUserList + '}';
	}

}
