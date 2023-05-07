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
import org.mockito.Mockito;

class NotificationMessageServiceTest {

	@Test
	void createNotificationMessage() {
		NotificationMessageRepository notificationMessageRepository = Mockito.mock(NotificationMessageRepository.class);
		NotificationUserService notificationUserService = Mockito.mock(NotificationUserService.class);
		NotificationRecordService notificationRecordService = Mockito.mock(NotificationRecordService.class);
		SecurityUtil securityUtil = Mockito.mock(SecurityUtil.class);

		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = Mockito
				.mock(NotificationMessageCreateRequestDTO.class);
		NotificationMessage savedNotificationMessage = Mockito.mock(NotificationMessage.class);
		NotificationUser notificationUser = Mockito.mock(NotificationUser.class);
		List<NotificationUser> usersToNotify = Collections.singletonList(notificationUser);
		List<String> usernames = Collections.singletonList("test-username");

		Mockito.when(notificationMessageCreateRequestDTO.getMessageType()).thenReturn("test-message-type");
		Mockito.when(notificationMessageCreateRequestDTO.getBody()).thenReturn("test-body");
		Mockito.when(notificationMessageCreateRequestDTO.getSubject()).thenReturn("test-subject");
		Mockito.when(notificationMessageCreateRequestDTO.getUsernames()).thenReturn(usernames);
		Mockito.when(notificationMessageCreateRequestDTO.getGroupnames())
				.thenReturn(Collections.singletonList("test-group-name"));
		Mockito.when(securityUtil.getUsername()).thenReturn("test-from-username");
		Mockito.when(notificationMessageRepository.save(Mockito.any())).thenReturn(savedNotificationMessage);
		Mockito.when(notificationUserService.findUsers(usernames)).thenReturn(usersToNotify);

		NotificationMessageService notificationMessageServiceImpl = new NotificationMessageServiceImpl(
				notificationMessageRepository, notificationUserService, notificationRecordService, securityUtil);
		notificationMessageServiceImpl.createNotificationMessage(notificationMessageCreateRequestDTO);

		Mockito.verify(notificationMessageCreateRequestDTO, Mockito.times(1)).getMessageType();
		Mockito.verify(notificationMessageCreateRequestDTO, Mockito.times(1)).getBody();
		Mockito.verify(notificationMessageCreateRequestDTO, Mockito.times(1)).getSubject();
		Mockito.verify(notificationMessageCreateRequestDTO, Mockito.times(2)).getUsernames();
		Mockito.verify(notificationMessageCreateRequestDTO, Mockito.times(1)).getGroupnames();
		Mockito.verify(securityUtil, Mockito.times(1)).getUsername();
		Mockito.verify(notificationMessageRepository, Mockito.times(1)).save(Mockito.any());
		Mockito.verify(notificationUserService, Mockito.times(1)).findUsers(usernames);
		Mockito.verify(notificationRecordService, Mockito.times(1)).createNotificationRecords(usersToNotify,
				savedNotificationMessage);
	}

}