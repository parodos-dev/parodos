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
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
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
public class NotificationGroupRepositoryTests extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationGroupRepository notificationGroupRepository;

	@Test
	void createFindDelete() {
		NotificationsDataCreator.createNotificationsGroup(notificationGroupRepository);
		List<NotificationGroup> groups = this.notificationGroupRepository.findAll();
		assertThat(groups).isNotEmpty();
		assertThat(groups).hasSize(1);
		NotificationGroup group = groups.get(0);
		assertThat(group.getGroupname()).isEqualTo(NotificationsDataCreator.ADMIN_GROUP);
		Optional<NotificationGroup> g = this.notificationGroupRepository.findById(group.getId());
		assertThat(g.isPresent());
		assertThat(g.get().getGroupname()).isEqualTo(NotificationsDataCreator.ADMIN_GROUP);
		Optional<NotificationGroup> g2 = this.notificationGroupRepository
				.findByGroupname(NotificationsDataCreator.ADMIN_GROUP);
		assertThat(g2.isPresent());
		assertThat(g2.get().getGroupname()).isEqualTo(NotificationsDataCreator.ADMIN_GROUP);
		this.notificationGroupRepository.deleteById(g2.get().getId());
		Optional<NotificationGroup> g3 = this.notificationGroupRepository.findById(group.getId());
		assertThat(g3.isEmpty());
	}

}
