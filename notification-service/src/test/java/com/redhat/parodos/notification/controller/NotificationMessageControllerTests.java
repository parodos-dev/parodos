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
package com.redhat.parodos.notification.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.jpa.NotificationsDataCreator;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationMessageRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.redhat.parodos.notification.jpa.NotificationsDataCreator.USER_A_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class NotificationMessageControllerTests extends AbstractControllerTests {

	@Autowired
	private NotificationMessageRepository notificationMessageRepository;

	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Autowired
	private NotificationGroupRepository notificationGroupRepository;

	@Autowired
	private NotificationRecordRepository notificationRecordRepository;

	@MockBean
	private SecurityUtil securityUtil;

	@TestConfiguration
	private class TestConfig {

		@Bean
		SecurityUtil securityUtil() {
			return securityUtil;
		}

	}

	@BeforeEach
	public void setup() {
		when(securityUtil.getUsername()).thenReturn(USER_A_1);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	void messageCreateAndNotifications() throws Exception {
		// Creates GROUP_A and GROUP_B with
		// users USER_A_1, USER_A2 in GROUP_A and USER_B_1,USER_B_2 in GROUP_B
		NotificationsDataCreator.createAndSaveTwoGroupsAndTwoUsersPerGroup(this.notificationGroupRepository,
				this.notificationUserRepository);

		NotificationMessageCreateRequestDTO createRequest = createRequestForUser1A();

		this.mockMvc.perform(postJson("/api/v1/messages", createRequest)).andDo(print())
				.andExpect(status().isCreated());

		List<NotificationMessage> notificationMessages = this.notificationMessageRepository.findAll();
		assertThat(notificationMessages).isNotEmpty();
		assertThat(notificationMessages).hasSize(1);
		NotificationMessage notificationMessage = notificationMessages.get(0);
		assertThat(notificationMessage.getFromuser()).isEqualTo(USER_A_1); // default user
																			// from
																			// @WithMockUser
		assertThat(notificationMessage.getBody()).isEqualTo(NotificationsDataCreator.MESSAGE_BODY);
		assertThat(notificationMessage.getSubject()).isEqualTo(NotificationsDataCreator.MESSAGE_SUBJECT);
		assertThat(notificationMessage.getMessageType()).isEqualTo(NotificationsDataCreator.MESSAGE_TYPE);
		assertThat(notificationMessage.getCreatedOn()).isCloseTo(Instant.now(), within(30, ChronoUnit.SECONDS));

		List<NotificationRecord> records = this.notificationRecordRepository.findAll();
		assertThat(records).isNotEmpty();
		assertThat(records).hasSize(1);

		NotificationRecord notificationRecord = records.get(0);

		assertThat(notificationRecord.isRead()).isEqualTo(false);
		assertThat(notificationRecord.getNotificationMessage().getBody())
				.isEqualTo(NotificationsDataCreator.MESSAGE_BODY);

		Optional<NotificationUser> notificationsUserOptional = this.notificationUserRepository.findByUsername(USER_A_1);
		NotificationUser notificationUser = notificationsUserOptional.get();

		Page<NotificationRecord> notificationsRecordPage = this.notificationRecordRepository
				.findByNotificationUserListContaining(notificationUser, PageRequest.of(0, 5));
		assertThat(notificationsRecordPage.getTotalElements()).isEqualTo(1);
		assertThat(notificationsRecordPage.getContent().get(0).getNotificationMessage().getBody())
				.isEqualTo(NotificationsDataCreator.MESSAGE_BODY);

		notificationsRecordPage = this.notificationRecordRepository
				.findByReadFalseAndNotificationUserListContaining(notificationUser, PageRequest.of(0, 5));
		assertThat(notificationsRecordPage.getTotalElements()).isEqualTo(1);
		assertThat(notificationsRecordPage.getContent().get(0).getNotificationMessage().getBody())
				.isEqualTo(NotificationsDataCreator.MESSAGE_BODY);

		Page<NotificationRecord> notificationsRecordPageReadTrue = this.notificationRecordRepository
				.findByReadTrueAndNotificationUserListContaining(notificationUser, PageRequest.of(0, 5));
		assertThat(notificationsRecordPageReadTrue.getTotalElements()).isEqualTo(0);

		NotificationRecord nr = notificationsRecordPage.getContent().get(0);
		nr.setRead(true);
		this.notificationRecordRepository.save(nr);

		Optional<NotificationRecord> nr2 = this.notificationRecordRepository.findById(nr.getId());
		assertThat(nr2.isPresent()).isTrue();

		Page<NotificationRecord> notificationsRecordsHasReadTrue = this.notificationRecordRepository
				.findByReadTrueAndNotificationUserListContaining(notificationUser, PageRequest.of(0, 5));
		assertThat(notificationsRecordsHasReadTrue.getTotalElements()).isEqualTo(1);

		assertThat(notificationsRecordsHasReadTrue.getContent().get(0).getNotificationMessage().getBody())
				.isEqualTo(NotificationsDataCreator.MESSAGE_BODY);
	}

}
