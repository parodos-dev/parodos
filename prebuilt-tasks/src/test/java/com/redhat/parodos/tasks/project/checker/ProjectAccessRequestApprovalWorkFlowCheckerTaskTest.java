package com.redhat.parodos.tasks.project.checker;

import java.util.UUID;

import com.redhat.parodos.project.enums.ProjectAccessStatus;
import com.redhat.parodos.tasks.project.dto.AccessStatusResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class ProjectAccessRequestApprovalWorkFlowCheckerTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String SERVICE_USERNAME_TEST = "service-username-test";

	private static final String SERVICE_PASSWORD_TEST = "service-password-test";

	private static final String ACCESS_REQUEST_ID_PARAMETER_NAME = "ACCESS_REQUEST_ID";

	private static final long SLA_TEST = 100L;

	@Mock
	private WorkFlow workFlow;

	private WorkContext workContext;

	private ProjectAccessRequestApprovalWorkFlowCheckerTask projectAccessRequestApprovalWorkFlowCheckerTask;

	@BeforeEach
	public void setUp() {
		this.projectAccessRequestApprovalWorkFlowCheckerTask = spy(new ProjectAccessRequestApprovalWorkFlowCheckerTask(
				workFlow, SLA_TEST, SERVICE_URL_TEST, SERVICE_USERNAME_TEST, SERVICE_PASSWORD_TEST));
		this.projectAccessRequestApprovalWorkFlowCheckerTask
				.setBeanName("projectAccessRequestApprovalWorkFlowCheckerTask");
		workContext = new WorkContext();
		WorkContextUtils.setProjectId(workContext, UUID.randomUUID());
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusCompleted() {
		UUID accessRequestId = UUID.randomUUID();

		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AccessStatusResponseDTO.builder().accessRequestId(accessRequestId)
							.status(ProjectAccessStatus.APPROVED).build()));
			WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusRejected() {
		UUID accessRequestId = UUID.randomUUID();

		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AccessStatusResponseDTO.builder().accessRequestId(accessRequestId)
							.status(ProjectAccessStatus.REJECTED).build()));
			WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
			assertEquals(WorkStatus.REJECTED, workReport.getStatus());
		}
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusPending() {
		UUID accessRequestId = UUID.randomUUID();

		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AccessStatusResponseDTO.builder().accessRequestId(accessRequestId)
							.status(ProjectAccessStatus.PENDING).build()));
			WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	@SneakyThrows
	public void executeFailForMissingRequiredParameter() {
		doThrow(MissingParameterException.class).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
