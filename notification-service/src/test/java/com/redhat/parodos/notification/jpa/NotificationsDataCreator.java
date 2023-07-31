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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.jpa.entity.NotificationGroup;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationMessageRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationRecordRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import org.modelmapper.ModelMapper;

/**
 * @author Richard Wang (Github: RichardW98)
 */
public final class NotificationsDataCreator {

	public static final String ARCHIVE_FOLDER = "archive";

	public static final String ADMIN_GROUP = "admin";

	public static final String GROUP_A = "group_a";

	public static final String GROUP_B = "group_b";

	public static final String USER_A_1 = "user_a_1";

	public static final String USER_A_2 = "user_a_2";

	public static final String USER_B_1 = "user_b_1";

	public static final String USER_B_2 = "user_b_2";

	public static final String MESSAGE_BODY = "this isn't so bad!";

	public static final String MESSAGE_SUBJECT = "please read this warning message";

	public static final String MESSAGE_TYPE = "warning";

	public static final String USERNAME = "johndoe";

	public static List<String> getTags() {
		List<String> tags = new ArrayList<>();
		tags.add("production");
		tags.add("system123");
		return tags;
	}

	public static void createNotificationsUser(NotificationUserRepository notificationUserRepository) {
		NotificationUser user = new NotificationUser();
		user.setUsername(USERNAME);
		notificationUserRepository.save(user);
	}

	public static void createNotificationsGroup(NotificationGroupRepository notificationGroupRepository) {
		NotificationGroup group = new NotificationGroup();
		group.setGroupname(ADMIN_GROUP);
		notificationGroupRepository.save(group);
	}

	// Creates GROUP_A and GROUP_B with
	// users USER_A_1, USER_A2 in GROUP_A and USER_B_1,USER_B_2 in GROUP_B
	public static void createAndSaveTwoGroupsAndTwoUsersPerGroup(
			NotificationGroupRepository notificationGroupRepository,
			NotificationUserRepository notificationUserRepository) {
		NotificationGroup group_a = new NotificationGroup();
		group_a.setGroupname(GROUP_A);
		notificationGroupRepository.save(group_a);

		NotificationGroup group_b = new NotificationGroup();
		group_b.setGroupname(GROUP_B);
		notificationGroupRepository.save(group_b);

		NotificationUser user_a_1 = new NotificationUser();
		user_a_1.setUsername(USER_A_1);
		user_a_1.addNotificationGroup(group_a);
		notificationUserRepository.save(user_a_1);

		NotificationUser user_a_2 = new NotificationUser();
		user_a_2.setUsername(USER_A_2);
		user_a_2.addNotificationGroup(group_a);
		notificationUserRepository.save(user_a_2);

		NotificationUser user_b_1 = new NotificationUser();
		user_b_1.setUsername(USER_B_1);
		user_b_1.addNotificationGroup(group_b);
		notificationUserRepository.save(user_b_1);

		NotificationUser user_b_2 = new NotificationUser();
		user_b_2.setUsername(USER_B_2);
		user_b_2.addNotificationGroup(group_b);
		notificationUserRepository.save(user_b_2);
	}

	public static void createNotificationsRecord(NotificationRecordRepository notificationRecordRepository) {
		NotificationRecord record = new NotificationRecord();
		record.setRead(false);
		record.setFolder(ARCHIVE_FOLDER);
		record.setTags(NotificationsDataCreator.getTags());
		notificationRecordRepository.save(record);
	}

	public static NotificationUser createNotificationsRecord(NotificationRecordRepository notificationRecordRepository,
			String subject, String messageBody, String username) {
		NotificationRecord record = new NotificationRecord();
		record.setRead(false);
		record.setFolder(ARCHIVE_FOLDER);
		record.setTags(NotificationsDataCreator.getTags());
		NotificationMessage notificationMessage = new NotificationMessage();
		notificationMessage.setBody(messageBody);
		notificationMessage.setSubject(subject);
		NotificationUser notificationUser = new NotificationUser();
		notificationUser.setUsername(username);
		record.getNotificationUserList().add(notificationUser);
		record.setNotificationMessage(notificationMessage);
		return notificationRecordRepository.save(record).getNotificationUserList().get(0);
	}

	public static void createAndSaveNotificationsMessage(NotificationMessageRepository notificationMessageRepository) {
		notificationMessageRepository.save(createNotificationsMessage());
	}

	public static NotificationMessage createNotificationsMessage() {
		NotificationMessage notificationMessage = new NotificationMessage();
		notificationMessage.setMessageType(MESSAGE_TYPE);
		notificationMessage.setBody(MESSAGE_BODY);
		notificationMessage.setCreatedOn(Instant.now());
		notificationMessage.setSubject(MESSAGE_SUBJECT);
		List<String> usernames = new ArrayList<>();
		usernames.add(NotificationsDataCreator.USERNAME);
		notificationMessage.setUsernames(usernames);
		return notificationMessage;
	}

	public static NotificationMessageCreateRequestDTO createNotificationsMessageRequest(List<String> usernames) {
		ModelMapper modelMapper = new ModelMapper();
		NotificationMessageCreateRequestDTO createRequest = modelMapper.map(createNotificationsMessage(),
				NotificationMessageCreateRequestDTO.class);
		createRequest.setUsernames(usernames);
		return createRequest;
	}

}
