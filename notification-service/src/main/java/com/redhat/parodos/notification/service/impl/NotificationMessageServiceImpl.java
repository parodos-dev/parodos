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

import com.redhat.parodos.notification.util.SecurityUtil;
import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationMessageRepository;
import com.redhat.parodos.notification.service.NotificationMessageService;
import com.redhat.parodos.notification.service.NotificationRecordService;
import com.redhat.parodos.notification.service.NotificationUserService;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Transactional
@Service
public class NotificationMessageServiceImpl implements NotificationMessageService {

	private final NotificationMessageRepository notificationMessageRepository;

	private final NotificationUserService notificationUserService;

	private final NotificationRecordService notificationRecordService;

	private final SecurityUtil securityUtil;

	public NotificationMessageServiceImpl(NotificationMessageRepository notificationMessageRepository,
			NotificationUserService notificationUserService, NotificationRecordService notificationRecordService,
			SecurityUtil securityUtil) {
		this.notificationMessageRepository = notificationMessageRepository;
		this.notificationUserService = notificationUserService;
		this.notificationRecordService = notificationRecordService;
		this.securityUtil = securityUtil;
	}

	@Override
	public void createNotificationMessage(NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO) {
		NotificationMessage notificationMessage = buildNotificationMessage(notificationMessageCreateRequestDTO);
		NotificationMessage savedNotificationMessage = this.notificationMessageRepository.save(notificationMessage);
		List<NotificationUser> usersToNotify = this.notificationUserService
				.findUsers(notificationMessageCreateRequestDTO.getUsernames());
		this.notificationRecordService.createNotificationRecords(usersToNotify, savedNotificationMessage);
	}

	private NotificationMessage buildNotificationMessage(
			NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO) {
		NotificationMessage notificationMessage = new NotificationMessage();
		notificationMessage.setMessageType(notificationMessageCreateRequestDTO.getMessageType());
		notificationMessage.setCreatedOn(Instant.now());
		notificationMessage.setBody(notificationMessageCreateRequestDTO.getBody());
		notificationMessage.setSubject(notificationMessageCreateRequestDTO.getSubject());
		notificationMessage.setUsernames(notificationMessageCreateRequestDTO.getUsernames());
		notificationMessage.setGroupnames(notificationMessageCreateRequestDTO.getGroupnames());
		notificationMessage.setFromuser(securityUtil.getUsername());
		return notificationMessage;
	}

}
