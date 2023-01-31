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

import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.controller.AbstractNotificationsIntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Richard Wang (Github: RichardW98)
 */

@Transactional
@ActiveProfiles("test")
public class NotificationUserRepositoryTests extends AbstractNotificationsIntegrationTest {
	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Test
	void createFindDelete() {
		NotificationsDataCreator.createNotificationsUser(notificationUserRepository);

		List<NotificationUser> users = this.notificationUserRepository.findAll();

		assertThat(users).isNotEmpty();
		assertThat(users).hasSize(1);

		NotificationUser user = users.get(0);
		assertThat(user.getUsername()).isEqualTo(NotificationsDataCreator.USERNAME);

		Optional<NotificationUser> u = this.notificationUserRepository.findById(user.getId());
		assertThat(u.isPresent());
		assertThat(u.get().getUsername()).isEqualTo(NotificationsDataCreator.USERNAME);

		Optional<NotificationUser> u2 = this.notificationUserRepository.findByUsername(NotificationsDataCreator.USERNAME);
		assertThat(u2.isPresent());
		assertThat(u2.get().getUsername()).isEqualTo(NotificationsDataCreator.USERNAME);

		this.notificationUserRepository.deleteById(u2.get().getId());

		Optional<NotificationUser> u3 = this.notificationUserRepository.findById(user.getId());
		assertThat(u3.isEmpty());
	}
}
