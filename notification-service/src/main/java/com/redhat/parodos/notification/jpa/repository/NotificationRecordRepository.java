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
package com.redhat.parodos.notification.jpa.repository;

import java.util.UUID;

import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Richard Wang (Github: RichardW98)
 */
@Repository
@Transactional
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, UUID> {

	Page<NotificationRecord> findByNotificationUserListContaining(NotificationUser user, Pageable pageable);

	Page<NotificationRecord> findByReadTrueAndNotificationUserListContaining(NotificationUser user, Pageable pageable);

	Page<NotificationRecord> findByReadFalseAndNotificationUserListContaining(NotificationUser user, Pageable pageable);

	int countDistinctByReadFalseAndNotificationUserListContaining(NotificationUser user);

	Page<NotificationRecord> findByFolderAndNotificationUserListContaining(String folder, NotificationUser user,
			Pageable pageable);

	@Query("Select notificationRecord from NotificationRecord notificationRecord where "
			+ " (LOWER(notificationRecord.notificationMessage.body) LIKE %:searchTerm% OR "
			+ " LOWER(notificationRecord.notificationMessage.messageType) LIKE %:searchTerm% OR "
			+ " LOWER(notificationRecord.notificationMessage.subject) LIKE %:searchTerm%) AND "
			+ " :user MEMBER OF notificationRecord.notificationUserList")
	Page<NotificationRecord> search(@Param("user") NotificationUser user, @Param("searchTerm") String searchTerm,
			Pageable pageable);

}
