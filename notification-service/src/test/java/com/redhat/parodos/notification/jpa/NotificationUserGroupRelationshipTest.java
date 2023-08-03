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
package com.redhat.parodos.notification.jpa;

import java.util.List;
import java.util.Optional;

import com.redhat.parodos.notification.controller.AbstractNotificationsIntegrationTest;
import com.redhat.parodos.notification.jpa.entity.NotificationGroup;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author Richard Wang (Github: RichardW98)
 */
@Transactional
@ActiveProfiles("test")
public class NotificationUserGroupRelationshipTest extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Autowired
	private NotificationGroupRepository notificationGroupRepository;

	@Test
	void userGroupRelationship() {
		NotificationsDataCreator.createNotificationsUser(notificationUserRepository);
		NotificationsDataCreator.createNotificationsGroup(notificationGroupRepository);

		Optional<NotificationUser> user = this.notificationUserRepository
				.findByUsername(NotificationsDataCreator.USERNAME);
		assertThat(user.isPresent(), is(true));

		Optional<NotificationGroup> group = this.notificationGroupRepository
				.findByGroupname(NotificationsDataCreator.ADMIN_GROUP);
		assertThat(group.isPresent(), is(true));

		// associate a group with a user
		user.get().addNotificationGroup(group.get());

		Optional<NotificationUser> u2 = this.notificationUserRepository
				.findByUsername(NotificationsDataCreator.USERNAME);
		assertThat(u2.isPresent(), is(true));
		List<NotificationGroup> notificationGroupList = u2.get().getNotificationGroupList();
		assertThat(notificationGroupList, hasSize(1));
		assertThat(notificationGroupList.get(0).getGroupname(), equalTo(NotificationsDataCreator.ADMIN_GROUP));

		// remove the group from the user
		u2.get().removeNotificationGroup(group.get());

		NotificationUser u3 = this.notificationUserRepository.save(u2.get());
		assertThat(u3.getNotificationGroupList(), empty());
	}

}
