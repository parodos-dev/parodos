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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.redhat.parodos.notification.jpa.entity.base.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Users belong to zero or more groups.
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Entity
@Table(name = "NotificationGroup")
@Setter
@Getter
public class NotificationGroup extends AbstractEntity {

	@Column(name = "groupname", unique = true)
	@NotNull
	private String groupname;

	@ManyToMany(mappedBy = "notificationGroupList", fetch = FetchType.LAZY)
	private List<NotificationUser> notificationUserList = new ArrayList<>();

	@Override
	public String toString() {
		return "NotificationGroup{" + "id=" + getId() + ", groupname='" + groupname + '\'' + ", notificationUserList="
				+ notificationUserList + '}';
	}

}
