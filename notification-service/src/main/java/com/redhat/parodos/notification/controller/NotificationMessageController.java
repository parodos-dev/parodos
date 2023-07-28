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

import jakarta.validation.Valid;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.service.NotificationMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification message controller
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@RestController
@RequestMapping("/api/v1/messages")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Notification Message", description = "Operations about notification message in the system")
public class NotificationMessageController {

	private final NotificationMessageService notificationMessageService;

	public NotificationMessageController(NotificationMessageService notificationMessageService) {
		this.notificationMessageService = notificationMessageService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody @Valid NotificationMessageCreateRequestDTO messageCreateRequest) {
		this.notificationMessageService.createNotificationMessage(messageCreateRequest);
	}

}
