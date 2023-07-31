package com.redhat.parodos.tasks.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NotificationWorkFlowTaskTest {

	private Notifier mockNotifier;

	private static NotificationWorkFlowTask underTest;

	private static WorkContext ctx;

	@BeforeEach
	public void setUp() {
		mockNotifier = mock(Notifier.class);
		underTest = new NotificationWorkFlowTask(mockNotifier);
		underTest.setBeanName("notificationWorkFlowTask");
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void testExecuteSuccess() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				"test-subject", Arrays.asList("test-user-1", "test-user-2"),
				Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(mockNotifier, times(1)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteApiCreateExceptionErr() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				"test-subject", Arrays.asList("test-user-1", "test-user-2"),
				Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);

		doThrow(ApiException.class).when(mockNotifier).trySend(dto);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(ApiException.class, result.getError().getClass());
		verify(mockNotifier, times(1)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingTypeErr() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO(null, "test-body",
				"test-subject", Arrays.asList("test-user-1", "test-user-2"),
				Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
		verify(mockNotifier, times(0)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingBodyErr() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", null,
				"test-subject", Arrays.asList("test-user-1", "test-user-2"),
				Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
		verify(mockNotifier, times(0)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingSubjectErr() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				null, Arrays.asList("test-user-1", "test-user-2"), Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
		verify(mockNotifier, times(0)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingUsernamesSuccess() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				"test-subject", null, Arrays.asList("test-group-1", "test-group-2"));
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(mockNotifier, times(1)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingGroupNamesSuccess() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				"test-subject", Arrays.asList("test-user-1", "test-user-2"), null);
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(mockNotifier, times(1)).trySend(dto);
	}

	@Test
	@SneakyThrows
	public void testExecuteMissingUsernamesAndGroupNamesErr() {
		NotificationMessageCreateRequestDTO dto = buildNotificationMessageCreateRequestDTO("test-type", "test-body",
				"test-subject", null, null);
		putParamsToCtx(dto, ctx);
		underTest.preExecute(ctx);
		WorkReport result = underTest.execute(ctx);

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
		verify(mockNotifier, times(0)).trySend(dto);
	}

	private NotificationMessageCreateRequestDTO buildNotificationMessageCreateRequestDTO(String type, String body,
			String subject, List<String> usernames, List<String> groupNames

	) {
		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();

		notificationMessageCreateRequestDTO.messageType(type);
		notificationMessageCreateRequestDTO.body(body);
		notificationMessageCreateRequestDTO.subject(subject);
		notificationMessageCreateRequestDTO.usernames(usernames);
		notificationMessageCreateRequestDTO.groupNames(groupNames);

		return notificationMessageCreateRequestDTO;
	}

	private void putParamsToCtx(NotificationMessageCreateRequestDTO dto, WorkContext ctx) {
		HashMap<String, String> map = new HashMap<>();

		putInMap(map, "type", dto.getMessageType());
		putInMap(map, "body", dto.getBody());
		putInMap(map, "subject", dto.getSubject());
		putInMap(map, "userNames", listToString(dto.getUsernames()));
		putInMap(map, "groupNames", listToString(dto.getGroupNames()));

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, underTest.getName(),
				WorkContextDelegate.Resource.ARGUMENTS, map);

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
	}

	private void putInMap(HashMap<String, String> map, String key, String value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	private String listToString(List<String> usernames) {
		if (usernames == null) {
			return null;
		}
		return String.join(";", usernames);
	}

}
