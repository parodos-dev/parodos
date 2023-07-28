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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.notification.controller.base.BaseControllerTests;
import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.exceptions.NotificationRecordNotFoundException;
import com.redhat.parodos.notification.exceptions.UnsupportedStateException;
import com.redhat.parodos.notification.exceptions.UsernameNotFoundException;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import com.redhat.parodos.notification.service.NotificationRecordService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("local")
class NotificationRecordControllerTests extends BaseControllerTests {

	@MockBean
	private NotificationRecordService notificationRecordService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void failListNoAuth() throws Exception {
		// when
		this.mockMvc
				.perform(getRequestWithInvalidCredentials("/api/v1/notifications").param("page", "1")
						.param("size", "100").param("sort", "id"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// then
		verify(this.notificationRecordService, never()).getNotificationRecords(any(), any(), any(), any());
	}

	@Test
	public void failListSortNotAllowed() throws Exception {
		// when
		this.mockMvc.perform(getRequestWithValidCredentials("/api/v1/notifications").param("page", "1")
				.param("size", "100").param("sort", "name")).andExpect(MockMvcResultMatchers.status().isBadRequest());

		// then
		verify(this.notificationRecordService, never()).getNotificationRecords(any(), any(), any(), any());
	}

	@Test
	public void failListUsernameNotFound() throws Exception {
		// given
		when(this.notificationRecordService.getNotificationRecords(any(), eq(getValidUser()), any(), any()))
				.thenThrow(new UsernameNotFoundException("test message"));

		// when
		this.mockMvc.perform(getRequestWithValidCredentials("/api/v1/notifications").param("page", "1")
				.param("size", "100").param("sort", "id")).andExpect(MockMvcResultMatchers.status().isNotFound());

		// then
		verify(this.notificationRecordService, times(1)).getNotificationRecords(any(), eq(getValidUser()), any(),
				any());
	}

	@Test
	public void successListEmptyResult() throws Exception {
		// given
		when(this.notificationRecordService.getNotificationRecords(any(), eq(getValidUser()), any(), any()))
				.thenReturn(Page.empty());

		// when
		this.mockMvc
				.perform(getRequestWithValidCredentials("/api/v1/notifications").param("page", "1").param("size", "100")
						.param("sort", "id"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());

		// then
		verify(this.notificationRecordService, times(1)).getNotificationRecords(any(), eq(getValidUser()), any(),
				any());
	}

	@Test
	public void successListWithValueResult() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		NotificationRecord notificationRecord = createMockNotificationRecord(id);
		when(this.notificationRecordService.getNotificationRecords(any(), eq(getValidUser()), any(), any()))
				.thenReturn(new PageImpl<>(List.of(notificationRecord)));

		// when
		this.mockMvc
				.perform(getRequestWithValidCredentials("/api/v1/notifications").param("page", "1").param("size", "100")
						.param("sort", "id"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", is(id.toString())));

		// then
		verify(this.notificationRecordService, times(1)).getNotificationRecords(any(), eq(getValidUser()), any(),
				any());
	}

	@Test
	public void failCountNoAuth() throws Exception {
		// given
		State state = State.UNREAD;

		// when
		this.mockMvc.perform(
				getRequestWithInvalidCredentials("/api/v1/notifications/count").param("state", state.toString()))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		verify(this.notificationRecordService, never()).countNotificationRecords(any(), eq(state));
	}

	@Test
	@WithMockUser
	public void failCountMissingState() throws Exception {
		// given
		int count = 4;
		when(this.notificationRecordService.countNotificationRecords(any(), any())).thenReturn(count);

		// when
		this.mockMvc.perform(getRequestWithValidCredentials("/api/v1/notifications/count"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		// then
		verify(this.notificationRecordService, never()).countNotificationRecords(any(), any());
	}

	@Test
	public void failCountWrongState() throws Exception {
		// given
		int count = 5;
		State state = State.UNREAD;
		when(this.notificationRecordService.countNotificationRecords(any(), eq(state)))
				.thenThrow(new UnsupportedStateException("test message"));

		// when
		this.mockMvc
				.perform(getRequestWithValidCredentials("/api/v1/notifications/count").param("state", state.toString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		// then
		verify(this.notificationRecordService, times(1)).countNotificationRecords(any(), eq(state));
	}

	@Test
	public void successCount() throws Exception {
		// given
		int count = 5;
		State state = State.UNREAD;
		when(this.notificationRecordService.countNotificationRecords(any(), eq(state))).thenReturn(count);

		// when
		this.mockMvc
				.perform(getRequestWithValidCredentials("/api/v1/notifications/count").param("state", state.toString()))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", is(count)));

		// then
		verify(this.notificationRecordService, times(1)).countNotificationRecords(any(), eq(state));
	}

	@Test
	public void failUpdateNoAuth() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		Operation operation = Operation.READ;

		// when
		this.mockMvc
				.perform(putRequestWithInvalidCredentials(String.format("/api/v1/notifications/%s", id))
						.param("operation", operation.toString()))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		verify(this.notificationRecordService, never()).updateNotificationStatus(any(), any());
	}

	@Test
	public void failUpdateBadOperation() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		Operation operation = Operation.READ;
		NotificationRecord notificationRecord = createMockNotificationRecord(id);
		when(this.notificationRecordService.updateNotificationStatus(eq(id), eq(operation)))
				.thenThrow(new UnsupportedOperationException("test message"));

		// when
		this.mockMvc
				.perform(putRequestWithValidCredentials(String.format("/api/v1/notifications/%s", id))
						.param("operation", operation.toString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		// then
		verify(this.notificationRecordService, times(1)).updateNotificationStatus(eq(id), eq(operation));
	}

	@Test
	public void failUpdateUserNotFound() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		Operation operation = Operation.READ;
		NotificationRecord notificationRecord = createMockNotificationRecord(id);
		when(this.notificationRecordService.updateNotificationStatus(eq(id), eq(operation)))
				.thenThrow(new NotificationRecordNotFoundException(id));

		// when
		this.mockMvc
				.perform(putRequestWithValidCredentials(String.format("/api/v1/notifications/%s", id))
						.param("operation", operation.toString()))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		// then
		verify(this.notificationRecordService, times(1)).updateNotificationStatus(eq(id), eq(operation));
	}

	@Test
	public void successUpdate() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		Operation operation = Operation.READ;
		NotificationRecord notificationRecord = createMockNotificationRecord(id);
		when(this.notificationRecordService.updateNotificationStatus(eq(id), eq(operation)))
				.thenReturn(notificationRecord);

		// when
		this.mockMvc
				.perform(putRequestWithValidCredentials(String.format("/api/v1/notifications/%s", id))
						.param("operation", operation.toString()))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(id.toString())));

		// then
		verify(this.notificationRecordService, times(1)).updateNotificationStatus(eq(id), eq(operation));
	}

	@Test
	public void failDeleteNoAuth() throws Exception {
		// given
		UUID id = UUID.randomUUID();

		// when
		this.mockMvc.perform(deleteRequestWithInvalidCredentials(String.format("/api/v1/notifications/%s", id)))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		verify(this.notificationRecordService, never()).deleteNotificationRecord(eq(id));
	}

	@Test
	public void successDelete() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		doNothing().when(this.notificationRecordService).deleteNotificationRecord(eq(id));

		// when
		this.mockMvc.perform(deleteRequestWithValidCredentials(String.format("/api/v1/notifications/%s", id)))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

		// then
		verify(this.notificationRecordService, times(1)).deleteNotificationRecord(eq(id));
	}

	private NotificationRecord createMockNotificationRecord(UUID id) {
		NotificationRecord notificationRecord = mock(NotificationRecord.class);
		NotificationMessage notificationMessage = mock(NotificationMessage.class);

		when(notificationRecord.getNotificationMessage()).thenReturn(notificationMessage);
		when(notificationRecord.getFolder()).thenReturn("testFolder");
		when(notificationRecord.isRead()).thenReturn(false);
		when(notificationRecord.getTags()).thenReturn(List.of("testTag"));
		when(notificationRecord.getId()).thenReturn(id);

		when(notificationMessage.getBody()).thenReturn("testBody");
		when(notificationMessage.getCreatedOn()).thenReturn(Instant.now());
		when(notificationMessage.getFromuser()).thenReturn("testUser");
		when(notificationMessage.getSubject()).thenReturn("testSubject");
		when(notificationMessage.getMessageType()).thenReturn("testType");

		return notificationRecord;
	}

}
