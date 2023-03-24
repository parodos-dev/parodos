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

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.redhat.parodos.notification.dto.NotificationAssemblerDTO;
import com.redhat.parodos.notification.dto.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.service.NotificationRecordService;
import com.redhat.parodos.notification.util.SecurityUtil;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Notification record controller
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
@Tag(name = "Notification Record", description = "Operations about notification record in the system")
public class NotificationRecordController {

	private final NotificationRecordService notificationRecordService;

	private final PagedResourcesAssembler<NotificationRecord> notificationRecordPagedResourcesAssembler;

	private final SecurityUtil securityUtil;

	private final NotificationAssemblerDTO notificationAssemblerDTO = new NotificationAssemblerDTO();

	public NotificationRecordController(NotificationRecordService notificationRecordService,
			PagedResourcesAssembler<NotificationRecord> notificationRecordPagedResourcesAssembler,
			SecurityUtil securityUtil) {
		this.notificationRecordService = notificationRecordService;
		this.notificationRecordPagedResourcesAssembler = notificationRecordPagedResourcesAssembler;
		this.securityUtil = securityUtil;
	}

	/**
	 * Returns all notifications for a user with option to apply filter or search term.
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedModel<NotificationRecordResponseDTO> getNotifications(@PageableDefault(size = 100) Pageable pageable,
			@RequestParam(value = "state", required = false) State state,
			@RequestParam(value = "searchTerm", required = false) String searchTerm) {
		if (securityUtil == null || securityUtil.getUsername() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No username associated with this request");
		}
		Page<NotificationRecord> notificationsRecordPage = this.notificationRecordService
				.getNotificationRecords(pageable, securityUtil.getUsername(), state, searchTerm);
		return this.notificationRecordPagedResourcesAssembler.toModel(notificationsRecordPage,
				this.notificationAssemblerDTO);
	}

	/**
	 * Returns unread notifications for a user.
	 */
	@GetMapping("/count")
	@ResponseStatus(HttpStatus.OK)
	public int countUnreadNotifications(@RequestParam State state) {
		return this.notificationRecordService.countNotificationRecords(securityUtil.getUsername(), state);
	}

	@PutMapping("/{id}")
	public NotificationRecordResponseDTO updateNotificationStatusById(@PathVariable("id") UUID id,
			@RequestParam Operation operation) {
		NotificationRecord notificationRecord = this.notificationRecordService.updateNotificationStatus(id, operation);
		return notificationAssemblerDTO.toModel(notificationRecord);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteNotification(@PathVariable("id") UUID id) {
		this.notificationRecordService.deleteNotificationRecord(id);
	}

}
