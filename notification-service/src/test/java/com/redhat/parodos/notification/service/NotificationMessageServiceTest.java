package com.redhat.parodos.notification.service;

import java.util.Collections;
import java.util.List;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.jpa.entity.NotificationMessage;
import com.redhat.parodos.notification.jpa.entity.NotificationUser;
import com.redhat.parodos.notification.jpa.repository.NotificationMessageRepository;
import com.redhat.parodos.notification.service.impl.NotificationMessageServiceImpl;
import com.redhat.parodos.notification.util.SecurityUtil;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationMessageServiceTest {

	@Test
	void createNotificationMessage() {
		NotificationMessageRepository notificationMessageRepository = mock(NotificationMessageRepository.class);
		NotificationUserService notificationUserService = mock(NotificationUserService.class);
		NotificationRecordService notificationRecordService = mock(NotificationRecordService.class);
		SecurityUtil securityUtil = mock(SecurityUtil.class);

		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = mock(
				NotificationMessageCreateRequestDTO.class);
		NotificationMessage savedNotificationMessage = mock(NotificationMessage.class);
		NotificationUser notificationUser = mock(NotificationUser.class);
		List<NotificationUser> usersToNotify = Collections.singletonList(notificationUser);
		List<String> usernames = Collections.singletonList("test-username");

		when(notificationMessageCreateRequestDTO.getMessageType()).thenReturn("test-message-type");
		when(notificationMessageCreateRequestDTO.getBody()).thenReturn("test-body");
		when(notificationMessageCreateRequestDTO.getSubject()).thenReturn("test-subject");
		when(notificationMessageCreateRequestDTO.getUsernames()).thenReturn(usernames);
		when(notificationMessageCreateRequestDTO.getGroupNames())
				.thenReturn(Collections.singletonList("test-group-name"));
		when(securityUtil.getUsername()).thenReturn("test-from-username");
		when(notificationMessageRepository.save(any())).thenReturn(savedNotificationMessage);
		when(notificationUserService.findUsers(usernames)).thenReturn(usersToNotify);

		NotificationMessageService notificationMessageServiceImpl = new NotificationMessageServiceImpl(
				notificationMessageRepository, notificationUserService, notificationRecordService, securityUtil);
		notificationMessageServiceImpl.createNotificationMessage(notificationMessageCreateRequestDTO);

		verify(notificationMessageCreateRequestDTO, times(1)).getMessageType();
		verify(notificationMessageCreateRequestDTO, times(1)).getBody();
		verify(notificationMessageCreateRequestDTO, times(1)).getSubject();
		verify(notificationMessageCreateRequestDTO, times(2)).getUsernames();
		verify(notificationMessageCreateRequestDTO, times(1)).getGroupNames();
		verify(securityUtil, times(1)).getUsername();
		verify(notificationMessageRepository, times(1)).save(any());
		verify(notificationUserService, times(1)).findUsers(usernames);
		verify(notificationRecordService, times(1)).createNotificationRecords(usersToNotify, savedNotificationMessage);
	}

}
