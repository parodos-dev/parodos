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

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Notification record service
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

public interface NotificationRecordService {

	/**
	 * Creates notification record with the provided notificationMessage for each
	 * notification user in notificationUsers
	 * @param notificationUsers List of all users to add new notification record with the
	 * notification message
	 * @param notificationMessage The notification message to be added
	 */
	void createNotificationRecords(List<NotificationUser> notificationUsers, NotificationMessage notificationMessage);

	/**
	 * Returns Notification Record according to the provided parameters
	 * @param pageable The representation of the set of pages to be printed
	 * @param username The notification username
	 * @param state The Notification state
	 * @param searchTerm A string value which can be used for the Notification Record
	 * query
	 * @return Page<NotificationRecord> - sublist of list of NotificationRecord. It allows
	 * gain information about the position of it in the containing entire list.
	 * @throws RuntimeException If user doesn't exist or if both state and searchTerm are
	 * not empty
	 */
	Page<NotificationRecord> getNotificationRecords(Pageable pageable, String username, State state, String searchTerm);

	/**
	 * @param username The notification repository username
	 * @param state The state of the records that should be counted, only UNREAD is
	 * supported
	 * @return The count of the unread records of the user, if the user exists, otherwise
	 * 0
	 * @throws RuntimeException If state is not READY
	 */
	int countNotificationRecords(String username, State state);

	/**
	 * Performs an action on the Notification Record represented by the provided ID,
	 * according to Operation
	 * @param id The Notification Record ID
	 * @param operation The operation should be done on the Notification Record
	 * @return The updated Notification Record
	 * @throws RuntimeException if operation is not supported
	 */
	NotificationRecord updateNotificationStatus(UUID id, Operation operation);

	/**
	 * Deletes the Notification Record represented by the provided ID
	 * @param id The Notification Record ID
	 */
	void deleteNotificationRecord(UUID id);

}
