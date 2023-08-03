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

import com.redhat.parodos.notification.controller.AbstractNotificationsIntegrationTest;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.repository.NotificationMessageRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Richard Wang (Github: RichardW98)
 */

@Transactional
@ActiveProfiles("test")
public class NotificationMessageRecordRelationshipTest extends AbstractNotificationsIntegrationTest {

	@Autowired
	private NotificationRecordRepository notificationRecordRepository;

	@Autowired
	private NotificationMessageRepository notificationMessageRepository;

	@Test
	void notificationRecordMessageRelationship() {
		NotificationsDataCreator.createNotificationsRecord(notificationRecordRepository);
		NotificationsDataCreator.createAndSaveNotificationsMessage(notificationMessageRepository);

		List<NotificationRecord> notificationRecords = this.notificationRecordRepository.findAll();
		assertThat(notificationRecords, not(empty()));
		assertThat(notificationRecords, hasSize(1));

		List<NotificationMessage> notificationMessages = this.notificationMessageRepository.findAll();
		assertThat(notificationMessages, not(empty()));
		assertThat(notificationMessages, hasSize(1));

		// associate the message with the notifications record (so the same message can be
		// across multiple notification records

		NotificationMessage notificationMessage = notificationMessages.get(0);
		NotificationRecord notificationRecord = notificationRecords.get(0);

		notificationRecord.setNotificationMessage(notificationMessage);
		// set the other side.
		// notificationsMessage.setNotificationsRecord(notificationsRecord);

		this.notificationRecordRepository.save(notificationRecord);

		List<NotificationRecord> notificationRecords2 = this.notificationRecordRepository.findAll();
		assertThat(notificationRecords2, not(empty()));
		assertThat(notificationRecords2, hasSize(1));
		NotificationRecord notificationRecord2 = notificationRecords2.get(0);
		assertThat(notificationRecord2.getNotificationMessage(), is(notNullValue()));
		// TODO
		// - notificationsRecord=null inside NotificationsMessage, why not bidirectional
		// - probably use @MapIds to be more efficient...
	}

}
