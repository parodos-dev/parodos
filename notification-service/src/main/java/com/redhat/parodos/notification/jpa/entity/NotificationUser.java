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
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user in the system, not definitive owner, can create a record per user
 * that authenticates to the system.
 * <p>
 * Users can belong to one or more groups
 * <p>
 * Users have a collection of notifications
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Entity
@Table(name = "NotificationUser")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationUser extends AbstractEntity {

	@Column(name = "username", unique = true)
	@NotNull
	private String username;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_group", joinColumns = @JoinColumn(name = "notificationuser_id"),
			inverseJoinColumns = @JoinColumn(name = "notificationgroup_id"))
	@Builder.Default
	private List<NotificationGroup> notificationGroupList = new ArrayList<>();

	@ManyToMany(mappedBy = "notificationUserList", fetch = FetchType.LAZY)
	@Builder.Default
	private List<NotificationRecord> notificationRecordList = new ArrayList<>();

	public void addNotificationGroup(NotificationGroup notificationGroup) {
		this.notificationGroupList.add(notificationGroup);
		notificationGroup.getNotificationUserList().add(this);
	}

	public void removeNotificationGroup(NotificationGroup notificationGroup) {
		this.notificationGroupList.remove(notificationGroup);
		notificationGroup.getNotificationUserList().remove(this);
	}

	public void addNotificationRecord(NotificationRecord notificationRecord) {
		this.notificationRecordList.add(notificationRecord);
		notificationRecord.getNotificationUserList().add(this);
	}

	public void removeNotificationRecord(NotificationRecord notificationRecord) {
		this.notificationRecordList.remove(notificationRecord);
		notificationRecord.getNotificationUserList().remove(this);
	}

	@Override
	public String toString() {
		return "NotificationUser{" + "id=" + getId() + ", username='" + username + '\'' + '}';
	}

}
