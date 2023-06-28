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
package com.redhat.parodos.notification.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.SearchCriteria;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.exceptions.NotificationRecordNotFoundException;
import com.redhat.parodos.notification.exceptions.UnsupportedStateException;
import com.redhat.parodos.notification.exceptions.UsernameNotFoundException;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.service.NotificationRecordService;
import com.redhat.parodos.notification.util.SearchUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Transactional
@Service
public class NotificationRecordServiceImpl implements NotificationRecordService {

	public static final String ARCHIVE_FOLDER = "archive";

	private final NotificationRecordRepository notificationRecordRepository;

	private final NotificationUserRepository notificationUserRepository;

	public NotificationRecordServiceImpl(NotificationRecordRepository notificationRecordRepository,
			NotificationUserRepository notificationUserRepository) {
		this.notificationRecordRepository = notificationRecordRepository;
		this.notificationUserRepository = notificationUserRepository;
	}

	@Override
	public void createNotificationRecords(List<NotificationUser> notificationUsers,
			NotificationMessage notificationMessage) {
		for (NotificationUser notificationUser : notificationUsers) {
			NotificationRecord record = new NotificationRecord();
			record.setRead(false);
			record.setNotificationMessage(notificationMessage);
			NotificationRecord savedNotificationRecord = this.notificationRecordRepository.save(record);
			// associate a notification record with a user
			notificationUser.addNotificationRecord(savedNotificationRecord);
			log.info("Saving notification record for user {}", notificationUser);
			this.notificationUserRepository.save(notificationUser);
		}
	}

	@Override
	public Page<NotificationRecord> getNotificationRecords(Pageable pageable, String username, State state,
			String searchTerm) {
		NotificationUser notificationUser = getNotificationUser(username);
		SearchCriteria searchCriteria = SearchUtil.getSearchCriteria(state, searchTerm);
		switch (searchCriteria) {
			case BY_USERNAME:
				return this.notificationRecordRepository.findByNotificationUserListContaining(notificationUser,
						pageable);
			case BY_USERNAME_AND_SEARCH_TERM:
				return this.notificationRecordRepository.search(notificationUser, searchTerm.toLowerCase(), pageable);
			case BY_USERNAME_AND_STATE_UNREAD:
				return this.notificationRecordRepository
						.findByReadFalseAndNotificationUserListContaining(notificationUser, pageable);
			case BY_USERNAME_AND_STATE_ARCHIVED:
				return this.notificationRecordRepository.findByFolderAndNotificationUserListContaining(ARCHIVE_FOLDER,
						notificationUser, pageable);
			default:
				return Page.empty();
		}
	}

	@Override
	public int countNotificationRecords(String username, State state) {
		if (Objects.requireNonNull(state) == State.UNREAD) {
			return this.notificationUserRepository.findByUsername(username)
					.map(notificationRecordRepository::countDistinctByReadFalseAndNotificationUserListContaining)
					.orElse(0);
		}
		throw new UnsupportedStateException(String.format("State %s is not supported", state));
	}

	@Override
	public NotificationRecord updateNotificationStatus(UUID id, Operation operation) {
		switch (operation) {
			case READ:
				return updateReadNotification(id);
			case ARCHIVE:
				return updateArchiveFolder(id);
			default:
				throw new UnsupportedOperationException(String.format("Operation %s is not supported", operation));
		}
	}

	@Override
	public void deleteNotificationRecord(UUID id) {
		try {
			this.notificationRecordRepository.deleteById(id);
		}
		catch (Exception e) {
			log.error("Deleting notification is failed for id: {}", id);
		}
	}

	private NotificationUser getNotificationUser(String username) {
		Optional<NotificationUser> notificationsUser = this.notificationUserRepository.findByUsername(username);
		if (notificationsUser.isEmpty()) {
			log.error("Unable to find the username:{}", username);
			throw new UsernameNotFoundException(username);
		}
		return notificationsUser.get();
	}

	private NotificationRecord updateReadNotification(UUID id) {
		NotificationRecord notificationRecord = findRecordById(id).get();
		notificationRecord.setRead(true);
		return this.notificationRecordRepository.save(notificationRecord);
	}

	private NotificationRecord updateArchiveFolder(UUID id) {
		NotificationRecord notificationRecord = findRecordById(id).get();
		notificationRecord.setFolder(ARCHIVE_FOLDER);
		return this.notificationRecordRepository.save(notificationRecord);
	}

	private Optional<NotificationRecord> findRecordById(UUID id) {
		Optional<NotificationRecord> notificationsRecordOptional = this.notificationRecordRepository.findById(id);
		if (notificationsRecordOptional.isEmpty()) {
			throw new NotificationRecordNotFoundException(id);
		}
		return notificationsRecordOptional;
	}

}
