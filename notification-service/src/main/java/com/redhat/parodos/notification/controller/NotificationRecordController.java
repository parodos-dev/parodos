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

import com.redhat.parodos.notification.dto.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.service.NotificationRecordService;
import com.redhat.parodos.notification.util.SecurityUtil;
import com.redhat.parodos.notification.validation.AllowedSortFields;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification record controller
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Notification Record", description = "Operations about notification record in the system")
public class NotificationRecordController {

	private final NotificationRecordService notificationRecordService;

	private final SecurityUtil securityUtil;

	public NotificationRecordController(NotificationRecordService notificationRecordService,
			SecurityUtil securityUtil) {
		this.notificationRecordService = notificationRecordService;
		this.securityUtil = securityUtil;
	}

	/**
	 * Returns all notifications for a user with option to apply filter or search term.
	 */
	@io.swagger.v3.oas.annotations.Operation(summary = "Return a list of notification records for the user")
	@ApiResponses(
			value = { @ApiResponse(responseCode = "200", description = "Successfully retrieved page of notifications"),
					@ApiResponse(responseCode = "400", description = "Bad Request"),
					@ApiResponse(responseCode = "401", description = "Unauthorized") })
	@GetMapping
	public ResponseEntity<Page<NotificationRecordResponseDTO>> getNotifications(
			@PageableDefault(size = 100) @AllowedSortFields({ "id", "notificationMessage.subject",
					"notificationMessage.fromuser", "notificationMessage.createdOn",
					"notificationMessage.messageType" }) @ParameterObject Pageable pageable,
			@RequestParam(value = "state", required = false) State state,
			@RequestParam(value = "searchTerm", required = false) String searchTerm) {

		Page<NotificationRecord> notificationsPage = this.notificationRecordService.getNotificationRecords(pageable,
				securityUtil.getUsername(), state, searchTerm);
		Page<NotificationRecordResponseDTO> notificationsDtoPage = notificationsPage
				.map(notificationRecord -> NotificationRecordResponseDTO.toModel(notificationRecord));
		return ResponseEntity.ok(notificationsDtoPage);
	}

	/**
	 * Returns the number of the notification records with given state for the user.
	 */
	@io.swagger.v3.oas.annotations.Operation(
			summary = "Return the number of the notification records with given state for the user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the amount of notifications"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	@GetMapping("/count")
	@ResponseStatus(HttpStatus.OK)
	public int countUnreadNotifications(@RequestParam State state) {
		return this.notificationRecordService.countNotificationRecords(securityUtil.getUsername(), state);
	}

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = NotificationRecordResponseDTO.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@io.swagger.v3.oas.annotations.Operation(summary = "Update the specified notification record with user operation")
	@PutMapping("/{id}")
	public NotificationRecordResponseDTO updateNotificationStatusById(@PathVariable("id") UUID id,
			@RequestParam Operation operation) {
		NotificationRecord notificationRecord = this.notificationRecordService.updateNotificationStatus(id, operation);
		return NotificationRecordResponseDTO.toModel(notificationRecord);
	}

	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Successfully Deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	@io.swagger.v3.oas.annotations.Operation(summary = "Delete the specified notification record")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteNotification(@PathVariable("id") UUID id) {
		this.notificationRecordService.deleteNotificationRecord(id);
	}

}
