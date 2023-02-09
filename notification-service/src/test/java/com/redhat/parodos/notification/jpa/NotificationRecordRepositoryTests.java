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
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
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
public class NotificationRecordRepositoryTests extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationRecordRepository notificationRecordRepository;

	@Test
	void createFindDelete() {
		NotificationsDataCreator.createNotificationsRecord(notificationRecordRepository);

		List<NotificationRecord> records = this.notificationRecordRepository.findAll();

		assertThat(records).isNotEmpty();
		assertThat(records).hasSize(1);

		NotificationRecord record = records.get(0);
		assertThat(record.getFolder()).isEqualTo(NotificationsDataCreator.ARCHIVE_FOLDER);

		Optional<NotificationRecord> r = this.notificationRecordRepository.findById(record.getId());
		assertThat(r.isPresent());
		assertThat(r.get().getFolder()).isEqualTo(NotificationsDataCreator.ARCHIVE_FOLDER);
		assertThat(r.get().getTags()).hasSize(2);

		this.notificationRecordRepository.deleteById(r.get().getId());

		Optional<NotificationRecord> r2 = this.notificationRecordRepository.findById(r.get().getId());
		assertThat(r2.isEmpty());
	}

}
