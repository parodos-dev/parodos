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

import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.service.NotificationUserService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Richard Wang (Github: RichardW98)
 */
@Transactional
@Service
public class NotificationUserServiceImpl implements NotificationUserService {

	private final NotificationUserRepository notificationUserRepository;

	public NotificationUserServiceImpl(NotificationUserRepository notificationUserRepository) {
		this.notificationUserRepository = notificationUserRepository;
	}

	@Override
	public List<NotificationUser> findUsers(List<String> usernames) {
		List<NotificationUser> notificationUsers = new ArrayList<>();
		for (String username : usernames) {
			notificationUsers.add(this.notificationUserRepository.findByUsername(username).orElse(
					NotificationUser.builder().username(username).notificationRecordList(new ArrayList<>()).build()));
		}
		return notificationUsers;
	}

}
