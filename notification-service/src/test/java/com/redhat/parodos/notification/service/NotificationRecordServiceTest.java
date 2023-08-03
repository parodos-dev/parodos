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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.exceptions.NotificationRecordNotFoundException;
import com.redhat.parodos.notification.exceptions.SearchByStateAndTermNotSupportedException;
import com.redhat.parodos.notification.exceptions.UnsupportedStateException;
import com.redhat.parodos.notification.exceptions.UsernameNotFoundException;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.service.impl.NotificationRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Nir Argaman (Github: nirarg)
 */

@Transactional
@ActiveProfiles("test")
public class NotificationRecordServiceTest {

	private NotificationRecordRepository notificationRecordRepository;

	private NotificationUserRepository notificationUserRepository;

	private NotificationRecordService notificationRecordServiceImpl;

	@BeforeEach
	void initNotificationRecordServiceImpl() {
		this.notificationRecordRepository = mock(NotificationRecordRepository.class);
		this.notificationUserRepository = mock(NotificationUserRepository.class);
		this.notificationRecordServiceImpl = new NotificationRecordServiceImpl(notificationRecordRepository,
				notificationUserRepository);
	}

	@Test
	void createNotificationRecords() {
		NotificationMessage notificationMessage = mock(NotificationMessage.class);
		NotificationUser notificationUser = mock(NotificationUser.class);
		List<NotificationUser> records = Collections.singletonList(notificationUser);
		NotificationRecord notificationRecord = mock(NotificationRecord.class);
		when(this.notificationRecordRepository.save(any())).thenReturn(notificationRecord);
		this.notificationRecordServiceImpl.createNotificationRecords(records, notificationMessage);
		verify(this.notificationRecordRepository, times(1)).save(any());
		verify(notificationUser, times(1)).addNotificationRecord(notificationRecord);
		verify(this.notificationUserRepository, times(1)).save(notificationUser);
	}

	@Test
	void getNotificationRecords() {
		String userName = "test-user";
		String searchTerm = "searchTerm";
		Pageable pageable = mock(Pageable.class);
		Optional<NotificationUser> emptyOptional = Optional.empty();
		NotificationUser notificationUser = mock(NotificationUser.class);
		Optional<NotificationUser> validOptional = Optional.of(notificationUser);
		NotificationRecord notificationRecord = mock(NotificationRecord.class);
		List<NotificationRecord> records = Collections.singletonList(notificationRecord);
		Page<NotificationRecord> expectedResult = new PageImpl<>(records);

		// Notification User doesn't exist
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(emptyOptional);
		Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
			this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName, State.UNREAD, searchTerm);
		});

		// Notification User exists, state and searchTerm empty
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
		when(this.notificationRecordRepository.findByNotificationUserListContaining(notificationUser, pageable))
				.thenReturn(expectedResult);
		Page<NotificationRecord> result = this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName,
				null, null);
		assertEquals(expectedResult, result);
		verify(this.notificationRecordRepository, times(1)).findByNotificationUserListContaining(notificationUser,
				pageable);

		reset(this.notificationRecordRepository);

		// Notification User exists, state UNREAD and searchTerm empty
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
		when(this.notificationRecordRepository.findByReadFalseAndNotificationUserListContaining(notificationUser,
				pageable)).thenReturn(expectedResult);
		result = this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName, State.UNREAD, null);
		assertEquals(expectedResult, result);
		verify(this.notificationRecordRepository, times(1))
				.findByReadFalseAndNotificationUserListContaining(notificationUser, pageable);

		reset(this.notificationRecordRepository);

		// Notification User exists, state ARCHIVED and searchTerm empty
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
		when(this.notificationRecordRepository.findByFolderAndNotificationUserListContaining("archive",
				notificationUser, pageable)).thenReturn(expectedResult);
		result = this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName, State.ARCHIVED, "");
		assertEquals(expectedResult, result);
		verify(this.notificationRecordRepository, times(1)).findByFolderAndNotificationUserListContaining("archive",
				notificationUser, pageable);

		reset(this.notificationRecordRepository);

		// Notification User exists, state empty and searchTerm valid
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
		when(this.notificationRecordRepository.search(notificationUser, searchTerm.toLowerCase(), pageable))
				.thenReturn(expectedResult);
		result = this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName, null, searchTerm);
		assertEquals(expectedResult, result);
		verify(this.notificationRecordRepository, times(1)).search(notificationUser, searchTerm.toLowerCase(),
				pageable);

		reset(this.notificationRecordRepository);

		// Notification User exists, state and searchTerm valid - exception expected
		when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
		exception = assertThrows(SearchByStateAndTermNotSupportedException.class, () -> {
			this.notificationRecordServiceImpl.getNotificationRecords(pageable, userName, State.UNREAD, searchTerm);
		});
		assertEquals("Search by state and search term combined not supported", exception.getMessage());
	}

	@Test
	void countNotificationRecords() {
		String userName = "test-user";

		for (State state : State.values()) {
			if (State.UNREAD.equals(state)) {
				// Test Notification User exists with 2 records
				NotificationUser notificationUser = mock(NotificationUser.class);
				NotificationRecord notificationRecord = mock(NotificationRecord.class);
				Optional<NotificationUser> validOptional = Optional.of(notificationUser);
				when(this.notificationUserRepository.findByUsername(userName)).thenReturn(validOptional);
				when(this.notificationRecordRepository
						.countDistinctByReadFalseAndNotificationUserListContaining(notificationUser)).thenReturn(2);
				List<NotificationRecord> records = Collections.singletonList(notificationRecord);
				when(notificationUser.getNotificationRecordList()).thenReturn(records);
				assertEquals(2, this.notificationRecordServiceImpl.countNotificationRecords(userName, state));

				// Test Notification User doesn't exist
				Optional<NotificationUser> emptyOptional = Optional.empty();
				when(this.notificationUserRepository.findByUsername(userName)).thenReturn(emptyOptional);
				assertEquals(0, this.notificationRecordServiceImpl.countNotificationRecords(userName, state));
			}
			else {
				// Test state value is not "UNREAD", exception is expected
				Exception exception = assertThrows(UnsupportedStateException.class, () -> {
					this.notificationRecordServiceImpl.countNotificationRecords(userName, state);
				});
				assertEquals(String.format("State %s is not supported", state), exception.getMessage());
			}
		}
	}

	@Test
	void updateNotificationStatus() {
		UUID uuid = UUID.randomUUID();
		Optional<NotificationRecord> emptyOptional = Optional.empty();
		NotificationRecord notificationRecord = mock(NotificationRecord.class);
		Optional<NotificationRecord> validOptional = Optional.of(notificationRecord);

		// Test each one of the operations when the record doesn't exist, exception is
		// expected
		for (Operation operation : Operation.values()) {
			when(this.notificationRecordRepository.findById(uuid)).thenReturn(emptyOptional);
			Exception exception = assertThrows(NotificationRecordNotFoundException.class, () -> {
				this.notificationRecordServiceImpl.updateNotificationStatus(uuid, operation);
			});
			assertEquals("Notification record not found: %s".formatted(uuid), exception.getMessage());
		}

		// Test READ operation and record exists
		when(this.notificationRecordRepository.findById(uuid)).thenReturn(validOptional);
		when(this.notificationRecordRepository.save(notificationRecord)).thenReturn(notificationRecord);
		NotificationRecord result = this.notificationRecordServiceImpl.updateNotificationStatus(uuid, Operation.READ);
		verify(notificationRecord, times(1)).setRead(true);
		assertEquals(notificationRecord, result);

		// Test ARCHIVE operation and record exists
		when(this.notificationRecordRepository.findById(uuid)).thenReturn(validOptional);
		when(this.notificationRecordRepository.save(notificationRecord)).thenReturn(notificationRecord);
		result = this.notificationRecordServiceImpl.updateNotificationStatus(uuid, Operation.ARCHIVE);
		verify(notificationRecord, times(1)).setFolder("archive");
		assertEquals(notificationRecord, result);
	}

	@Test
	void deleteNotificationRecord() {
		UUID uuid = UUID.randomUUID();

		// Delete success
		this.notificationRecordServiceImpl.deleteNotificationRecord(uuid);
		verify(this.notificationRecordRepository, times(1)).deleteById(uuid);

		reset(this.notificationRecordRepository);

		// Delete failure
		doThrow(RuntimeException.class).when(this.notificationRecordRepository).deleteById(uuid);
		this.notificationRecordServiceImpl.deleteNotificationRecord(uuid);
		verify(this.notificationRecordRepository, times(1)).deleteById(uuid);
	}

}
