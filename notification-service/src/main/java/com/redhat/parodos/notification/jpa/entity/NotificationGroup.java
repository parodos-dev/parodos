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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;

/**
 * Users belong to zero or more groups.
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Entity
@Table(name = "NotificationGroup")
public class NotificationGroup extends AbstractEntity {

	@Column(name = "groupname", unique = true)
	@NotNull
	private String groupname;

	@ManyToMany(mappedBy = "notificationGroupList", fetch = FetchType.LAZY)
	private List<NotificationUser> notificationUserList = new ArrayList<>();

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public List<NotificationUser> getNotificationsUserList() {
		return notificationUserList;
	}

	public void setNotificationsUserList(List<NotificationUser> notificationUserList) {
		this.notificationUserList = notificationUserList;
	}

	@Override
	public String toString() {
		return "NotificationGroup{" + "id=" + getId() + ", groupname='" + groupname + '\'' + ", notificationUserList="
				+ notificationUserList + '}';
	}

}
