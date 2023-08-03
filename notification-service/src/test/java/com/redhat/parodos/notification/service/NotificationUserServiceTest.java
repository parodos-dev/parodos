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
package com.redhat.parodos.notification.service;

import java.util.ArrayList;
import java.util.List;

import com.redhat.parodos.notification.controller.AbstractNotificationsIntegrationTest;
import com.redhat.parodos.notification.jpa.NotificationsDataCreator;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Transactional
@ActiveProfiles("test")
public class NotificationUserServiceTest extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Autowired
	private NotificationGroupRepository notificationGroupRepository;

	@Autowired
	private NotificationUserService notificationUserService;

	@Test
	void findUsers() {
		NotificationsDataCreator.createAndSaveTwoGroupsAndTwoUsersPerGroup(notificationGroupRepository,
				notificationUserRepository);
		List<String> usernames = new ArrayList<>();
		usernames.add(NotificationsDataCreator.USER_A_1);
		usernames.add(NotificationsDataCreator.USER_A_2);
		List<NotificationUser> usersToNotify = this.notificationUserService.findUsers(usernames);
		assertThat(usersToNotify, not(empty()));
		assertThat(usersToNotify, hasSize(2));
	}

}
