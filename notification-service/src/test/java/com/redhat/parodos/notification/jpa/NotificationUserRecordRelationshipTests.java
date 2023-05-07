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
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
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
public class NotificationUserRecordRelationshipTests extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Autowired
	private NotificationRecordRepository notificationRecordRepository;

	@Test
	void userRecordRelationship() {
		NotificationsDataCreator.createNotificationsUser(notificationUserRepository);
		NotificationsDataCreator.createNotificationsRecord(notificationRecordRepository);

		// Sanity check
		Optional<NotificationUser> userOptional = this.notificationUserRepository
				.findByUsername(NotificationsDataCreator.USERNAME);
		assertThat(userOptional.isPresent());

		List<NotificationRecord> notificationRecords = this.notificationRecordRepository.findAll();
		assertThat(notificationRecords).isNotEmpty();
		assertThat(notificationRecords).hasSize(1);

		NotificationUser user = userOptional.get();

		// associate a notification record with a user
		user.addNotificationRecord(notificationRecords.get(0));

		this.notificationUserRepository.save(user);

		Optional<NotificationUser> u2 = this.notificationUserRepository
				.findByUsername(NotificationsDataCreator.USERNAME);
		assertThat(u2.isPresent());
		List<NotificationRecord> notificationRecordList = u2.get().getNotificationRecordList();
		assertThat(notificationRecordList).hasSize(1);
		assertThat(notificationRecordList.get(0).getFolder()).isEqualTo(NotificationsDataCreator.ARCHIVE_FOLDER);
		assertThat(notificationRecordList.get(0).getTags()).hasSameElementsAs(NotificationsDataCreator.getTags());
	}

}
